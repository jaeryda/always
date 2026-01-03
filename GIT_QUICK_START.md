# Git 빠른 시작 가이드

## 1. Git 사용자 정보 설정 (최초 1회만)

```powershell
# 사용자 이름 설정
git config --global user.name "Your Name"

# 이메일 설정  
git config --global user.email "your.email@example.com"
```

**예시:**
```powershell
git config --global user.name "jykim"
git config --global user.email "jykim@example.com"
```

## 2. 현재 상태 확인

```powershell
git status
```

## 3. 첫 커밋하기

```powershell
# 모든 파일 추가
git add .

# 커밋
git commit -m "Initial commit: Always 프로젝트 (가계부 기능 추가)"
```

## 4. 원격 저장소 연결 (GitHub 사용 시)

### GitHub에서 새 저장소 생성 후:

```powershell
# 원격 저장소 추가 (GitHub 저장소 URL로 변경)
git remote add origin https://github.com/your-username/always.git

# 브랜치 이름을 main으로 변경 (최신 Git 기본값)
git branch -M main

# 첫 푸시
git push -u origin main
```

## 5. Jenkins와 연동

Jenkins에서 로컬 Git 저장소를 사용하려면:

1. Jenkins Job 설정
2. **Source Code Management** → **None** 선택 (로컬 파일 시스템 사용)
3. 또는 **Git** 선택 → Repository URL에 로컬 경로: `file:///C:/workspace/always`

## 현재 상태

✅ Git 저장소 초기화됨  
✅ .gitignore 파일 있음  
⚠️ Git 사용자 이메일 설정 필요 (위 1번 참조)  
⚠️ 아직 커밋하지 않음

