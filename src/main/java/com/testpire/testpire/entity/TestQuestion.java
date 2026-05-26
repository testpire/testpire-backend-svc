package com.testpire.testpire.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "test_questions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_id", "question_id"})
})
public class TestQuestion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  private Integer points;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @CreationTimestamp
  @Column(name = "added_at", updatable = false)
  private LocalDateTime addedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "added_by")
  private User addedBy;
}