package ua.com.fm.school;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DatabaseInitializer {

	public static void dbCreation() {
		createTables();
		generateTestData();
	}

	private static void createTables() {
		try {
			String createTablesSql = new String(Files.readAllBytes(Paths.get("create_tables.sql")));

			try (Connection connection = DatabaseConnection.getInstance().getConnection();
					Statement statement = connection.createStatement()) {

				statement.execute(createTablesSql);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateTestData() {
		generateGroups();
		generateCourses();
		generateStudents();
		generateStudentCourses();
	}

	private static void generateGroups() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection();
				Statement statement = connection.createStatement()) {

			Random random = new Random();

			for (int i = 1; i <= 10; i++) {
				String groupName = generateRandomGroupName();
				String insertGroupSql = String.format("INSERT INTO groups(group_name) VALUES ('%s')", groupName);
				statement.execute(insertGroupSql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String[] COURSE_NAMES = { "Math", "Biology", "Physics", "History", "Chemistry", "Literature",
			"Computer Science", "Art", "Music", "Physical Education" };

	private static void generateCourses() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection();
				Statement statement = connection.createStatement()) {

			for (String courseName : COURSE_NAMES) {
				String insertCourseSql = String.format(
						"INSERT INTO courses(course_name, course_description) VALUES ('%s', 'Description for %s')",
						courseName, courseName);
				statement.execute(insertCourseSql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateStudents() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection();
				Statement statement = connection.createStatement()) {

			String[] firstNames = { "John", "Emma", "Michael", "Olivia", "William", "Sophia", "James", "Ava", "Oliver",
					"Isabella" };
			String[] lastNames = { "Smith", "Johnson", "Williams", "Jones", "Brown", "Miller", "Davis", "Garcia",
					"Rodriguez", "Martinez" };

			Random random = new Random();

			for (int i = 1; i <= 200; i++) {
				String firstName = firstNames[random.nextInt(firstNames.length)];
				String lastName = lastNames[random.nextInt(lastNames.length)];
				int groupId = random.nextInt(10) + 1;

				String insertStudentSql = String.format(
						"INSERT INTO students(group_id, first_name, last_name) VALUES (%d, '%s', '%s')", groupId,
						firstName, lastName);
				statement.execute(insertStudentSql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String generateRandomGroupName() {
		Random random = new Random();
		int num1 = random.nextInt(100);
		int num2 = random.nextInt(100);
		return String.format("Group-%02d-%02d", num1, num2);
	}

	private static void generateStudentCourses() {
		try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
			Random random = new Random();

			Map<Integer, Set<Integer>> studentCourseAssignments = new HashMap<>();

			String selectCoursesQuery = "SELECT course_id FROM courses";
			try (PreparedStatement coursesStatement = connection.prepareStatement(selectCoursesQuery);
					ResultSet coursesResultSet = coursesStatement.executeQuery()) {
				while (coursesResultSet.next()) {
					int courseId = coursesResultSet.getInt("course_id");

					String selectStudentsQuery = "SELECT student_id FROM students";
					try (PreparedStatement studentsStatement = connection.prepareStatement(selectStudentsQuery);
							ResultSet studentsResultSet = studentsStatement.executeQuery()) {
						while (studentsResultSet.next()) {
							int studentId = studentsResultSet.getInt("student_id");

							if (studentCourseAssignments.computeIfAbsent(studentId, k -> new HashSet<>())
									.add(courseId)) {
								String insertStudentCourseSql = String.format(
										"INSERT INTO student_courses(student_id, course_id) VALUES (%d, %d)", studentId,
										courseId);
								try (PreparedStatement insertStatement = connection
										.prepareStatement(insertStudentCourseSql)) {
									insertStatement.execute();
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
