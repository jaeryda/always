package com.always.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    private Long id;

    private String name;  // 메뉴 이름 (예: "홈", "포스트", "About")

    private String path;  // 라우터 경로 (예: "/", "/posts", "/about")

    private String icon;  // 아이콘 이름 (예: "Home", "Document", "InfoFilled")

    private Integer displayOrder;  // 표시 순서

    private Boolean visible = true;  // 표시 여부

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
