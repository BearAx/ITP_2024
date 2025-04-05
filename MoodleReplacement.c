#include <stdio.h>  // Include standard libraries that we need
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#define MAX_STUDENTS 100  // Define maximum number of students
#define MAX_EXAMS 100     // Define maximum number of exams
#define MAX_NAME_LENGTH 100  // Define maximum length for student name
#define MAX_FACULTY_LENGTH 100  // Define maximum length for faculty name
#define MAX_TYPE_LENGTH 20  // Define maximum length for exam type
#define MAX_COMMAND_LENGTH 256  // Define maximum length for command

// Structure to store student data
typedef struct {
    int id;  // Student ID
    char name[MAX_NAME_LENGTH];  // Student name
    char faculty[MAX_FACULTY_LENGTH];  // Faculty name
} Student;

// Structure to store exam data
typedef struct {
    int id;  // Exam ID
    char type[MAX_TYPE_LENGTH];  // Exam type (e.g., WRITTEN or DIGITAL)
    char info[MAX_NAME_LENGTH];  // Additional exam information
} Exam;

// Structure to store grade data
typedef struct {
    int exam_id;  // Exam ID
    int student_id;  // Student ID
    int grade;  // Grade value
} Grade;

Student students[MAX_STUDENTS];  // Array to store students
Exam exams[MAX_EXAMS];  // Array to store exams
Grade grades[MAX_EXAMS * MAX_STUDENTS];  // Array to store grades

int student_count = 0;  // Number of students added
int exam_count = 0;  // Number of exams added
int grade_count = 0;  // Number of grades added

FILE *output;  // Output file pointer

// Function to find a student by ID
int find_student(int id) {
    for (int i = 0; i < student_count; i++) {
        if (students[i].id == id) {
            return i;  // Return index if student is found
        }
    }
    return -1;  // Return -1 if student is not found
}

// Function to find an exam by ID
int find_exam(int id) {
    for (int i = 0; i < exam_count; i++) {
        if (exams[i].id == id) {
            return i;  // Return index if exam is found
        }
    }
    return -1;  // Return -1 if exam is not found
}

// Function to add a new student
void add_student(int id, char *name, char *faculty) {
    if (find_student(id) != -1) {
        fprintf(output, "Student: %d already exists\n", id);
        return;  // Do not add if student ID already exists
    }
    if (strlen(name) >= MAX_NAME_LENGTH || strlen(faculty) >= MAX_FACULTY_LENGTH) {
        fprintf(output, "Invalid name or faculty length\n");
        return;  // Check for valid length of name and faculty
    }
    // Validate faculty name
    if (strcmp(faculty, "SoftwareEngineering") != 0 &&
        strcmp(faculty, "ComputerScience") != 0 &&
        strcmp(faculty, "DataScience") != 0 &&
        strcmp(faculty, "CyberSecurity") != 0 &&
        strcmp(faculty, "InformationTechnology") != 0 &&
        strcmp(faculty, "ProgrammingLanguagesAndCompilers") != 0) {
        fprintf(output, "Invalid faculty\n");
        return;  // Check for valid faculty name
    }
    // Ensure name contains only alphabetic characters
    for (int i = 0; name[i] != '\0'; i++) {
        if (!isalpha(name[i])) {
            fprintf(output, "Invalid name\n");
            return;  // If name contains non-alphabetical characters, reject it
        }
    }
    // Add the new student
    students[student_count].id = id;
    strcpy(students[student_count].name, name);
    strcpy(students[student_count].faculty, faculty);
    student_count++;
    fprintf(output, "Student: %d added\n", id);
}

// Function to add a new exam
void add_exam(int id, char *type, char *info) {
    if (find_exam(id) != -1) {
        fprintf(output, "Exam: %d already exists\n", id);
        return;  // Do not add if exam ID already exists
    }
    if (strlen(type) >= MAX_TYPE_LENGTH || strlen(info) >= MAX_NAME_LENGTH) {
        fprintf(output, "Invalid type or info length\n");
        return;  // Check for valid length of type and info
    }
    // Add the new exam
    exams[exam_count].id = id;
    strcpy(exams[exam_count].type, type);
    strcpy(exams[exam_count].info, info);
    exam_count++;
    fprintf(output, "Exam: %d added\n", id);
}

// Function to add a grade for a student in an exam
void add_grade(int exam_id, int student_id, int grade_value) {
    if (grade_value < 0 || grade_value > 100) {
        fprintf(output, "Invalid grade\n");
        return;  // Grade value must be between 0 and 100
    }
    if (find_student(student_id) == -1) {
        fprintf(output, "Student not found\n");
        return;  // Ensure student exists
    }
    if (find_exam(exam_id) == -1) {
        fprintf(output, "Exam not found\n");
        return;  // Ensure exam exists
    }
    // Add the new grade
    grades[grade_count].exam_id = exam_id;
    grades[grade_count].student_id = student_id;
    grades[grade_count].grade = grade_value;
    grade_count++;
    fprintf(output, "Grade %d added for the student: %d\n", grade_value, student_id);
}

// Function to update exam information
void update_exam(int id, char *new_type, char *new_info) {
    int index = find_exam(id);
    if (index == -1) {
        fprintf(output, "Exam not found\n");
        return;  // Ensure exam exists
    }

    // Validate the new type of exam before updating
    if (strcmp(new_type, "WRITTEN") != 0 && strcmp(new_type, "DIGITAL") != 0) {
        fprintf(output, "Invalid exam type\n");
        return;  // Type must be either WRITTEN or DIGITAL
    }

    // Update the exam type and information
    strcpy(exams[index].type, new_type);
    strcpy(exams[index].info, new_info);
    fprintf(output, "Exam: %d updated\n", id);
}

// Function to update a grade
void update_grade(int exam_id, int student_id, int new_grade) {
    if (new_grade < 0 || new_grade > 100) {
        fprintf(output, "Invalid grade\n");
        return;  // Grade value must be between 0 and 100
    }
    for (int i = 0; i < grade_count; i++) {
        if (grades[i].exam_id == exam_id && grades[i].student_id == student_id) {
            grades[i].grade = new_grade;
            fprintf(output, "Grade %d updated for the student: %d\n", new_grade, student_id);
            return;  // Update the grade if found
        }
    }
    fprintf(output, "Student not found\n");
}

// Function to delete a student
void delete_student(int id) {
    int index = find_student(id);
    if (index == -1) {
        fprintf(output, "Student not found\n");
        return;  // Ensure student exists
    }
    // Remove all grades associated with the student
    for (int i = 0; i < grade_count; i++) {
        if (grades[i].student_id == id) {
            for (int j = i; j < grade_count - 1; j++) {
                grades[j] = grades[j + 1];  // Shift grades left
            }
            grade_count--;
            i--;  // Recheck the current position after shifting
        }
    }
    // Remove the student from the array
    for (int i = index; i < student_count - 1; i++) {
        students[i] = students[i + 1];  // Shift students left
    }
    student_count--;
    fprintf(output, "Student: %d deleted\n", id);
}

// Function to search and display student information
void search_student(int id) {
    int index = find_student(id);
    if (index == -1) {
        fprintf(output, "Student not found\n");
        return;  // Ensure student exists
    }
    fprintf(output, "ID: %d, Name: %s, Faculty: %s\n", students[index].id, students[index].name, students[index].faculty);
}

// Function to search and display grade information
void search_grade(int exam_id, int student_id) {
    int student_index = find_student(student_id);
    if (student_index == -1) {
        fprintf(output, "Student not found\n");
        return;  // Ensure student exists
    }
    for (int i = 0; i < grade_count; i++) {
        if (grades[i].exam_id == exam_id && grades[i].student_id == student_id) {
            int exam_index = find_exam(exam_id);
            if (exam_index == -1) {
                fprintf(output, "Exam not found\n");
                return;  // Ensure exam exists
            }
            fprintf(output, "Exam: %d, Student: %d, Name: %s, Grade: %d, Type: %s, Info: %s\n",
                    exam_id, student_id, students[student_index].name, grades[i].grade,
                    exams[exam_index].type, exams[exam_index].info);
            return;  // Display grade information if found
        }
    }
    fprintf(output, "Grade not found\n");
}

// Function to list all students
void list_all_students() {
    for (int i = 0; i < student_count; i++) {
        fprintf(output, "ID: %d, Name: %s, Faculty: %s\n", students[i].id, students[i].name, students[i].faculty);
    }
}

int main() {
    FILE *input = fopen("input.txt", "r");  // Open input file in reading mode
    if (!input) {
        perror("Failed to open input file");
        return 1;  // Return 1 if input file cannot be opened
    }

    output = fopen("output.txt", "w");  // Open output file in writing mode
    if (!output) {
        perror("Failed to open output file");
        fclose(input);
        return 1;  // Return 1 if output file cannot be opened
    }

    char command[MAX_COMMAND_LENGTH];  // Command buffer
    while (fgets(command, sizeof(command), input)) {
        char cmd[30];  // Command name buffer
        int id1, id2, grade;
        char name[MAX_NAME_LENGTH], faculty[MAX_FACULTY_LENGTH];
        char type[MAX_TYPE_LENGTH], info[MAX_NAME_LENGTH];

        // Extract the command
        sscanf(command, "%s", cmd);

        if (strcmp(cmd, "ADD_STUDENT") == 0) {
            // Extract parameters for the ADD_STUDENT command
            if (sscanf(command, "%*s %d %s %s", &id1, name, faculty) == 3) {
                add_student(id1, name, faculty);
            } else {
                fprintf(output, "Invalid ADD_STUDENT command format\n");
            }
        } else if (strcmp(cmd, "ADD_EXAM") == 0) {
            if (sscanf(command, "%*s %d %s %s", &id1, type, info) == 3) {
                add_exam(id1, type, info);
            } else {
                fprintf(output, "Invalid ADD_EXAM command format\n");
            }
        } else if (strcmp(cmd, "ADD_GRADE") == 0) {
            if (sscanf(command, "%*s %d %d %d", &id1, &id2, &grade) == 3) {
                add_grade(id1, id2, grade);
            } else {
                fprintf(output, "Invalid ADD_GRADE command format\n");
            }
        } else if (strcmp(cmd, "UPDATE_EXAM") == 0) {
            if (sscanf(command, "%*s %d %s %s", &id1, type, info) == 3) {
                update_exam(id1, type, info);
            } else {
                fprintf(output, "Invalid UPDATE_EXAM command format\n");
            }
        } else if (strcmp(cmd, "UPDATE_GRADE") == 0) {
            if (sscanf(command, "%*s %d %d %d", &id1, &id2, &grade) == 3) {
                update_grade(id1, id2, grade);
            } else {
                fprintf(output, "Invalid UPDATE_GRADE command format\n");
            }
        } else if (strcmp(cmd, "DELETE_STUDENT") == 0) {
            if (sscanf(command, "%*s %d", &id1) == 1) {
                delete_student(id1);
            } else {
                fprintf(output, "Invalid DELETE_STUDENT command format\n");
            }
        } else if (strcmp(cmd, "SEARCH_STUDENT") == 0) {
            if (sscanf(command, "%*s %d", &id1) == 1) {
                search_student(id1);
            } else {
                fprintf(output, "Invalid SEARCH_STUDENT command format\n");
            }
        } else if (strcmp(cmd, "SEARCH_GRADE") == 0) {
            if (sscanf(command, "%*s %d %d", &id1, &id2) == 2) {
                search_grade(id1, id2);
            } else {
                fprintf(output, "Invalid SEARCH_GRADE command format\n");
            }
        } else if (strcmp(cmd, "LIST_ALL_STUDENTS") == 0) {
            list_all_students();
        } else if (strcmp(cmd, "END") == 0) {
            // End processing commands
            break;
        } else {
            fprintf(output, "Unknown command: %s\n", cmd);
        }
    }

    fclose(input);  // Close input file
    fclose(output);  // Close output file
    return 0;
}