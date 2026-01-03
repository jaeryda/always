import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko' // 한국어 로케일

// dayjs 플러그인 확장
dayjs.extend(relativeTime)

// dayjs를 한국어로 설정
dayjs.locale('ko')

/**
 * 날짜를 한국어 형식으로 포맷팅
 * @param dateString - ISO 날짜 문자열 또는 undefined
 * @param format - dayjs 포맷 문자열 (기본값: 'YYYY.MM.DD HH:mm')
 * @returns 포맷된 날짜 문자열, 입력이 없으면 빈 문자열 반환
 */
export const formatDate = (dateString: string | undefined, format = 'YYYY.MM.DD HH:mm'): string => {
  if (!dateString) return ''
  
  const date = dayjs(dateString)
  if (!date.isValid()) return ''
  
  return date.format(format)
}

/**
 * 날짜를 상대 시간으로 포맷팅 (예: "2시간 전", "3일 전")
 * @param dateString - ISO 날짜 문자열 또는 undefined
 * @returns 상대 시간 문자열
 */
export const formatRelativeTime = (dateString: string | undefined): string => {
  if (!dateString) return ''
  
  const date = dayjs(dateString)
  if (!date.isValid()) return ''
  
  return date.fromNow()
}

/**
 * 날짜를 상세 형식으로 포맷팅 (예: "2024년 1월 15일 14시 30분")
 * @param dateString - ISO 날짜 문자열 또는 undefined
 * @returns 상세 포맷된 날짜 문자열
 */
export const formatDateDetailed = (dateString: string | undefined): string => {
  return formatDate(dateString, 'YYYY년 MM월 DD일 HH시 mm분')
}

/**
 * 날짜를 간단한 형식으로 포맷팅 (예: "2024.01.15")
 * @param dateString - ISO 날짜 문자열 또는 undefined
 * @returns 간단한 포맷된 날짜 문자열
 */
export const formatDateShort = (dateString: string | undefined): string => {
  return formatDate(dateString, 'YYYY.MM.DD')
}

