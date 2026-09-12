/**
 * Xuất dữ liệu ra file CSV kèm UTF-8 BOM để tương thích hoàn hảo với Microsoft Excel và các phần mềm bảng tính.
 *
 * @param {string} filename Tên file xuất (vd: danh-sach-chuyen-xe.csv)
 * @param {Array<{ label: string, key?: string, format?: (row: any) => any }>} columns Cấu hình các cột
 * @param {Array<any>} data Mảng dữ liệu cần xuất
 */
export const exportToCSV = (filename, columns, data = []) => {
  if (!Array.isArray(data) || data.length === 0) {
    throw new Error('Không có dữ liệu để xuất')
  }

  // 1. Tiêu đề các cột
  const headers = columns.map(c => `"${(c.label || '').replace(/"/g, '""')}"`).join(',')

  // 2. Nội dung các dòng
  const rows = data.map(row => {
    return columns.map(col => {
      let val = ''
      if (typeof col.format === 'function') {
        val = col.format(row)
      } else if (col.key) {
        val = row[col.key]
      }
      if (val === null || val === undefined) {
        val = ''
      }
      const strVal = String(val).replace(/"/g, '""')
      return `"${strVal}"`
    }).join(',')
  })

  // 3. Kết hợp với UTF-8 BOM (\uFEFF)
  const csvContent = '\uFEFF' + [headers, ...rows].join('\r\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)

  // 4. Kích hoạt tải về tự động
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename.endsWith('.csv') ? filename : `${filename}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
