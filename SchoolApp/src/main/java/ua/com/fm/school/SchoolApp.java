package ua.com.fm.school;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

public class SchoolApp {

	public static void main(String[] args) {
		DatabaseInitializer.dbCreation();
		Scanner scanner = new Scanner(System.in);
		int choice;

		do {
			System.out.println("1. Find all groups with less or equal students’ number");
			System.out.println("2. Find all students related to the course with the given name");
			System.out.println("3. Add a new student");
			System.out.println("4. Delete a student by the STUDENT_ID");
			System.out.println("5. Add a student to the course (from a list)");
			System.out.println("6. Remove the student from one of their courses");
			System.out.println("0. Exit");

			System.out.print("Enter your choice: ");
			choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				findAllGroupsWithLessOrEqualStudents();
				break;
			case 2:
				findAllStudentsByCourseName();
				break;
			case 3:
				addNewStudent();
				break;
			case 4:
				deleteStudentById();
				break;
			case 5:
				addStudentToCourse();
				break;
			case 6:
				removeStudentFromCourse();
				break;
			case 0:
				System.out.println("Exiting the program.");
				break;
			default:
				System.out.println("Invalid choice. Please enter a valid option.");
			}
			System.out.println();
		} while (choice != 0);

		scanner.close();
	}
	private static boolean isStudentExists(Connection connection, int studentId) {
	    try {
	        String checkStudentExistsSql = "SELECT * FROM students WHERE student_id = ?";
	        try (PreparedStatement preparedStatement = connection.prepareStatement(checkStudentExistsSql)) {
	            preparedStatement.setInt(1, studentId);

	            try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                return resultSet.next();
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	private static void findAllGroupsWithLessOrEqualStudents() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
			String query = "SELECT groups.group_id, group_name, COUNT(students.student_id) AS num_students "
					+ "FROM groups LEFT JOIN students ON groups.group_id = students.group_id "
					+ "GROUP BY groups.group_id, group_name " + "HAVING COUNT(students.student_id) <= ?";

			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				Scanner inputScanner = new Scanner(System.in);
				System.out.print("Enter the maximum number of students: ");
				int maxStudents = inputScanner.nextInt();

				preparedStatement.setInt(1, maxStudents);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					System.out.println("\nGroups with less or equal students' number:");
					System.out.printf("%-10s %-20s %-15s%n", "Group ID", "Group Name", "Num Students");
					System.out.println("-----------------------------------------");

					boolean foundGroups = false;
					while (resultSet.next()) {
						int groupId = resultSet.getInt("group_id");
						String groupName = resultSet.getString("group_name");
						int numStudents = resultSet.getInt("num_students");

						System.out.printf("%-10d %-20s %-15d%n", groupId, groupName, numStudents);
						foundGroups = true;
					}

					if (!foundGroups) {
						System.out.println("No groups found with less or equal students' number.");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void findAllStudentsByCourseName() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
			String courseQuery = "SELECT course_name FROM courses";

			try (PreparedStatement courseStatement = connection.prepareStatement(courseQuery);
					ResultSet courseResultSet = courseStatement.executeQuery()) {

				System.out.println("Available course names:");
				Set<String> availableCourses = new HashSet<>();
				while (courseResultSet.next()) {
					String courseName = courseResultSet.getString("course_name");
					availableCourses.add(courseName);
					System.out.println(courseName);
				}

				Scanner inputScanner = new Scanner(System.in);
				System.out.print("Enter the course name: ");
				String courseName = inputScanner.nextLine();

				if (!availableCourses.contains(courseName)) {
					System.out.println("The entered course '" + courseName + "' does not exist.");
					return;
				}

				String query = "SELECT students.student_id, first_name, last_name, groups.group_name "
						+ "FROM students " + "JOIN student_courses ON students.student_id = student_courses.student_id "
						+ "JOIN courses ON student_courses.course_id = courses.course_id "
						+ "JOIN groups ON students.group_id = groups.group_id " + "WHERE courses.course_name = ?";

				try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
					preparedStatement.setString(1, courseName);

					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						System.out.println("\nStudents related to the course '" + courseName + "':");
						System.out.printf("%-10s %-15s %-15s %-20s%n", "Student ID", "First Name", "Last Name",
								"Group Name");
						System.out.println("----------------------------------------------");

						boolean foundStudents = false;
						while (resultSet.next()) {
							int studentId = resultSet.getInt("student_id");
							String firstName = resultSet.getString("first_name");
							String lastName = resultSet.getString("last_name");
							String groupName = resultSet.getString("group_name");

							System.out.printf("%-10d %-15s %-15s %-20s%n", studentId, firstName, lastName, groupName);
							foundStudents = true;
						}

						if (!foundStudents) {
							System.out.println("No students found related to the course '" + courseName + "'.");
						}
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void addNewStudent() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
			Scanner inputScanner = new Scanner(System.in);

			System.out.print("Enter the student's first name: ");
			String firstName = inputScanner.next();

			System.out.print("Enter the student's last name: ");
			String lastName = inputScanner.next();

			try {
				System.out.print("Enter the student's group ID: ");
				int groupId = inputScanner.nextInt();

				String insertStudentSql = "INSERT INTO students(first_name, last_name, group_id) VALUES (?, ?, ?)";
				try (PreparedStatement preparedStatement = connection.prepareStatement(insertStudentSql)) {
					preparedStatement.setString(1, firstName);
					preparedStatement.setString(2, lastName);
					preparedStatement.setInt(3, groupId);
					preparedStatement.executeUpdate();

					System.out.println("Student added successfully.");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Please enter a valid group ID.");
				inputScanner.nextLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void deleteStudentById() {
	    try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
	        Scanner inputScanner = new Scanner(System.in);

	        System.out.print("Enter the STUDENT_ID of the student to delete: ");
	        int studentId = inputScanner.nextInt();

	        if (isStudentExists(connection, studentId)) {
	            String deleteStudentCoursesSql = "DELETE FROM student_courses WHERE student_id = ?";
	            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteStudentCoursesSql)) {
	                preparedStatement.setInt(1, studentId);
	                preparedStatement.executeUpdate();
	            }

	            String deleteStudentSql = "DELETE FROM students WHERE student_id = ?";
	            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteStudentSql)) {
	                preparedStatement.setInt(1, studentId);

	                int rowsAffected = preparedStatement.executeUpdate();

	                if (rowsAffected > 0) {
	                    System.out.println("Student with STUDENT_ID " + studentId + " deleted successfully.");
	                } else {
	                    System.out.println("No student found with STUDENT_ID " + studentId + ".");
	                }
	            }
	        } else {
	            System.out.println("No student found with STUDENT_ID " + studentId + ".");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	private static void addStudentToCourse() {
	    try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
	        Scanner inputScanner = new Scanner(System.in);

	        displayAvailableCourses(connection);

	        try {
	            System.out.print("Enter the course ID to add a student to: ");
	            int courseId = inputScanner.nextInt();

	            displayAvailableStudents(connection);

	            System.out.print("Enter the student ID to add to the course: ");
	            int studentId = inputScanner.nextInt();

	            if (isStudentExists(connection, studentId)) {
	                if (isStudentInCourse(connection, studentId, courseId)) {
	                    System.out.println("Student is already enrolled in the course.");
	                } else {
	                    String addStudentToCourseSql = "INSERT INTO student_courses(student_id, course_id) VALUES (?, ?)";
	                    try (PreparedStatement preparedStatement = connection.prepareStatement(addStudentToCourseSql)) {
	                        preparedStatement.setInt(1, studentId);
	                        preparedStatement.setInt(2, courseId);
	                        preparedStatement.executeUpdate();

	                        System.out.println("Student added to the course successfully.");
	                    } catch (SQLException e) {
	                        e.printStackTrace();
	                    }
	                }
	            } else {
	                System.out.println("No student found with STUDENT_ID " + studentId + ".");
	            }
	        } catch (InputMismatchException e) {
	            System.out.println("Invalid input. Please enter a valid course ID and student ID.");
	            inputScanner.nextLine();
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	private static void displayAvailableCourses(Connection connection) {
		try {
			String selectCoursesSql = "SELECT course_id, course_name FROM courses";
			try (PreparedStatement preparedStatement = connection.prepareStatement(selectCoursesSql);
					ResultSet resultSet = preparedStatement.executeQuery()) {

				System.out.println("Available course names:");
				while (resultSet.next()) {
					int courseId = resultSet.getInt("course_id");
					String courseName = resultSet.getString("course_name");
					System.out.println(courseId + "\t" + courseName);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void displayAvailableStudents(Connection connection) {
		try {
			String selectStudentsSql = "SELECT student_id, first_name, last_name FROM students";
			try (PreparedStatement preparedStatement = connection.prepareStatement(selectStudentsSql);
					ResultSet resultSet = preparedStatement.executeQuery()) {

				System.out.println("Available students:");
				while (resultSet.next()) {
					int studentId = resultSet.getInt("student_id");
					String firstName = resultSet.getString("first_name");
					String lastName = resultSet.getString("last_name");
					System.out.println(studentId + "\t" + firstName + "\t" + lastName);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static boolean isStudentInCourse(Connection connection, int studentId, int courseId) {
		try {
			String checkStudentInCourseSql = "SELECT * FROM student_courses WHERE student_id = ? AND course_id = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(checkStudentInCourseSql)) {
				preparedStatement.setInt(1, studentId);
				preparedStatement.setInt(2, courseId);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					return resultSet.next();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void removeStudentFromCourse() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
			Scanner inputScanner = new Scanner(System.in);

			System.out.print("Enter the student's ID: ");
			int studentId = inputScanner.nextInt();

			System.out.print("Enter the course ID to remove the student from: ");
			int courseId = inputScanner.nextInt();

			String checkEnrollmentSql = "SELECT * FROM student_courses WHERE student_id = ? AND course_id = ?";
			try (PreparedStatement checkEnrollmentStatement = connection.prepareStatement(checkEnrollmentSql)) {
				checkEnrollmentStatement.setInt(1, studentId);
				checkEnrollmentStatement.setInt(2, courseId);

				ResultSet resultSet = checkEnrollmentStatement.executeQuery();

				if (!resultSet.next()) {
					System.out.println("The student is not enrolled in the specified course.");
					return;
				}
			}

			String removeStudentFromCourseSql = "DELETE FROM student_courses WHERE student_id = ? AND course_id = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(removeStudentFromCourseSql)) {
				preparedStatement.setInt(1, studentId);
				preparedStatement.setInt(2, courseId);
				int rowsAffected = preparedStatement.executeUpdate();

				if (rowsAffected > 0) {
					System.out.println("Student removed from the course successfully.");
				} else {
					System.out.println("Failed to remove the student from the course.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}