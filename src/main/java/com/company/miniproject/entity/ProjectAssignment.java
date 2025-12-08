package com.company.miniproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Entity
@Table(name = "project_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "employee_id"}, name = "uk_project_employee")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"project", "employee"})
@EqualsAndHashCode(exclude = {"project", "employee"})
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "role_in_project", nullable = false, length = 30)
    private String roleInProject;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    public ProjectAssignment(Project project, Employee employee, String roleInProject, LocalDate joinDate) {
        this.project = project;
        this.employee = employee;
        this.roleInProject = roleInProject;
        this.joinDate = joinDate;
    }
}