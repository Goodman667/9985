package com.example.phq9assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class Phq9AssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(Phq9AssessmentApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("🚀 智能PHQ-9评估系统启动成功！");
        System.out.println("🤖 AI/ML功能已激活");
        System.out.println("📊 访问地址: http://localhost:8080");
        System.out.println("========================================\n");
    }

}
