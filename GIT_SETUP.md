# Git 설정 가이드

## 1. Git 사용자 정보 설정 (최초 1회만)

Git을 처음 사용하는 경우, 사용자 이름과 이메일을 설정해야 합니다:

```powershell
# 사용자 이름 설정
git config --global user.name "Your Name"

# 이메일 설정
git config --global user.email "your.email@example.com"
```

### 예시:
```powershell
git config --global user.name "홍길동"
git config --global user.email "hong@example.com"
```

## 2. Git 저장소 초기화 (이미 완료됨)

현재 프로젝트는 이미 Git 저장소로 초기화되어 있습니다.
필요한 경우 다시 초기화하려면:

```powershell
cd c:\workspace\always
git init
```

## 3. .gitignore 확인

프로젝트에 `.gitignore` 파일이 있습니다. 다음 파일들은 Git에서 제외됩니다:
- `node_modules/` - Node.js 패키지
- `target/` - Maven 빌드 결과물
- `dist/` - Vue 빌드 결과물
- `*.log` - 로그 파일
- IDE 설정 파일들

## 4. 첫 커밋하기

### 4-1. 모든 파일 추가
```powershell
cd c:\workspace\always
git add .
```

### 4-2. 커밋
```powershell
git commit -m "Initial commit: Always 프로젝트"
```

## 5. Git 저장소 확인

```powershell
# 현재 상태 확인
git status

# 커밋 내역 확인
git log

# 브랜치 확인
git branch
```

## 6. 원격 저장소 연결 (GitHub 등)

### 6-1. GitHub에서 새 저장소 생성
1. GitHub에서 새 Repository 생성
2. Repository URL 복사 (예: `https://github.com/username/always.git`)

### 6-2. 원격 저장소 추가
```powershell
# 원격 저장소 추가
git remote add origin https://github.com/username/always.git

# 원격 저장소 확인
git remote -v
```

### 6-3. 첫 푸시
```powershell
# main 브랜치로 푸시
git branch -M main
git push -u origin main
```

## 7. Jenkins와 연동하기

Jenkins에서 Git 저장소를 사용하려면:

### 7-1. 로컬 Git 저장소 경로 사용
Jenkins Job 설정에서:
- **Source Code Management** → **None** (로컬 파일 시스템 사용)
- 또는 **Git** → Repository URL에 로컬 경로: `file:///c:/workspace/always`

### 7-2. GitHub/GitLab 사용
Jenkins Job 설정에서:
- **Source Code Management** → **Git**
- Repository URL: `https://github.com/username/always.git`
- Credentials: GitHub 인증 정보 추가

## 8. 기본 Git 명령어

```powershell
# 상태 확인
git status

# 변경사항 스테이징
git add .
git add 특정파일

# 커밋
git commit -m "커밋 메시지"

# 원격 저장소에 푸시
git push

# 원격 저장소에서 가져오기
git pull

# 브랜치 생성
git branch 브랜치이름

# 브랜치 전환
git checkout 브랜치이름
```

## 9. 현재 프로젝트 상태

현재 프로젝트는:
- ✅ Git 저장소로 초기화됨 (master 브랜치)
- ✅ .gitignore 파일 있음
- ⚠️ 아직 커밋하지 않음

다음 단계:
1. Git 사용자 정보 설정 (위 1번 참조)
2. `git add .` 로 파일 추가
3. `git commit -m "Initial commit"` 로 첫 커밋

