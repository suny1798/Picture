import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 * @param size
 */
export const formatSize = (size?: number): string => {
  if (!size) return '未知'

  const KB = 1024
  const MB = KB * 1024
  const GB = MB * 1024

  if (size < KB) {
    return size + ' B'
  }
  if (size < MB) {
    return (size / KB).toFixed(2) + ' KB'
  }
  if (size < GB) {
    return (size / MB).toFixed(2) + ' MB'
  }
  return (size / GB).toFixed(2) + ' GB'
}

/**
 * 下载图片
 * @param url 图片下载地址
 * @param fileName 要保存为的文件名
 */
export function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  saveAs(url, fileName)
}
