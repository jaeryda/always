package com.always.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    // 이미지 저장 경로 (Windows)
    private final String uploadDir = "C:\\Users\\jy_kim\\Pictures\\server_picture";

    public FileService() {
        // 디렉토리가 없으면 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * 이미지 파일 저장
     * @param file 업로드된 파일
     * @return 저장된 파일의 이름 (UUID_원본파일명)
     */
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 원본 파일명
        String originalFilename = file.getOriginalFilename();
        
        // 파일 확장자 확인
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // UUID로 고유한 파일명 생성
        String savedFilename = UUID.randomUUID().toString() + extension;
        
        // 저장 경로
        Path targetPath = Paths.get(uploadDir, savedFilename);
        
        // 파일 저장
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return savedFilename;
    }

    /**
     * 이미지 파일 조회
     * @param filename 파일명
     * @return 파일 경로
     */
    public Path getImagePath(String filename) {
        Path filePath = Paths.get(uploadDir, filename);
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("파일을 찾을 수 없습니다: " + filename);
        }
        
        return filePath;
    }

    /**
     * 이미지 파일 삭제
     * @param filename 파일명
     */
    public void deleteImage(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        
        Path filePath = Paths.get(uploadDir, filename);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    /**
     * 업로드 디렉토리 경로 반환
     */
    public String getUploadDir() {
        return uploadDir;
    }
}

