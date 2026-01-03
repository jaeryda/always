# OpenAI API 통합 가이드

OpenAI API를 프로젝트에 통합하는 방법입니다.

## 1. OpenAI API 키 발급

1. [OpenAI Platform](https://platform.openai.com/)에 접속하여 계정을 생성합니다.
2. 대시보드에서 **API Keys** 섹션으로 이동합니다.
3. **Create new secret key**를 클릭하여 새로운 API 키를 생성합니다.
4. 생성된 API 키를 안전한 곳에 저장합니다. (한 번만 표시됩니다)

## 2. API 키 설정

### Windows (PowerShell)

```powershell
# 환경 변수로 설정 (현재 세션)
$env:OPENAI_API_KEY = "your-api-key-here"

# 시스템 환경 변수로 영구 설정
[System.Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "your-api-key-here", [System.EnvironmentVariableTarget]::User)
```

### Linux/Mac

```bash
# 환경 변수로 설정 (현재 세션)
export OPENAI_API_KEY="your-api-key-here"

# 영구 설정 (.bashrc 또는 .zshrc에 추가)
echo 'export OPENAI_API_KEY="your-api-key-here"' >> ~/.bashrc
source ~/.bashrc
```

### 또는 application.properties에 직접 설정 (개발용만)

`always-svc/src/main/resources/application-mysql.properties` 파일에서:

```properties
openai.api.key=your-api-key-here
```

⚠️ **주의**: 프로덕션 환경에서는 환경 변수를 사용하세요. API 키를 코드에 직접 포함하지 마세요.

## 3. API 사용 방법

### 백엔드 (Java)

```java
@Autowired
private OpenAIService openAIService;

public void example() {
    String prompt = "안녕하세요, OpenAI API를 사용해봅시다.";
    String result = openAIService.generateCompletion(prompt);
    System.out.println(result);
}
```

### 프론트엔드 (Vue/TypeScript)

```typescript
import { openaiApi } from '@/api/openai'

const handleCompletion = async () => {
  try {
    const response = await openaiApi.generateCompletion({
      prompt: '안녕하세요, OpenAI API를 사용해봅시다.',
      maxTokens: 500,
      temperature: 0.7
    })
    
    if (response.data.success) {
      console.log('결과:', response.data.result)
    }
  } catch (error) {
    console.error('오류:', error)
  }
}
```

## 4. API 엔드포인트

### POST `/api/openai/completion`

OpenAI Completion을 생성합니다.

**Request Body:**
```json
{
  "prompt": "안녕하세요, OpenAI API를 사용해봅시다.",
  "maxTokens": 500,
  "temperature": 0.7
}
```

**Response:**
```json
{
  "success": true,
  "message": "완료되었습니다.",
  "result": "안녕하세요! OpenAI API를 사용해보시는군요...",
  "timestamp": "2025-12-28T19:30:00"
}
```

**Parameters:**
- `prompt` (required): 프롬프트 텍스트
- `maxTokens` (optional): 최대 토큰 수 (기본값: 500)
- `temperature` (optional): 생성 온도 (0.0 ~ 2.0, 기본값: 0.7)

## 5. 요금 정보

OpenAI API는 사용량에 따라 요금이 부과됩니다. 자세한 가격 정보는 [OpenAI Pricing](https://openai.com/pricing)를 확인하세요.

## 6. 문제 해결

### "OpenAI API key가 설정되지 않았습니다" 오류

- 환경 변수 `OPENAI_API_KEY`가 설정되었는지 확인하세요.
- 서버를 재시작했는지 확인하세요.
- `application.properties`에 직접 키를 설정한 경우, 파일이 올바른 위치에 있는지 확인하세요.

### API 호출 실패

- API 키가 유효한지 확인하세요.
- OpenAI 계정에 충분한 크레딧이 있는지 확인하세요.
- 네트워크 연결을 확인하세요.

