// @ts-ignore
import { saveAs } from 'file-saver'
import { Document, Packer, Paragraph, HeadingLevel, Table, TableRow, TableCell, WidthType } from 'docx'
import type { TeachingContentResponse, LessonContent } from '@/api/ai'

/**
 * 导出为Word文档
 */
export async function exportToWord(content: TeachingContentResponse, courseName: string) {
    const doc = new Document({
        sections: [{
            properties: {},
            children: [
                // 标题
                new Paragraph({
                    text: `${courseName} - 教学内容设计`,
                    heading: HeadingLevel.TITLE,
                    spacing: {
                        after: 200
                    }
                }),

                // 总体信息
                new Paragraph({
                    text: '总体教学安排',
                    heading: HeadingLevel.HEADING_1,
                    spacing: { before: 400, after: 200 }
                }),
                new Paragraph({
                    text: `总课时：${content.totalHours}`,
                    spacing: { after: 200 }
                }),
                new Paragraph({
                    text: '时间分配建议：',
                    spacing: { after: 100 }
                }),
                new Paragraph({
                    text: content.timeDistribution,
                    spacing: { after: 200 }
                }),
                new Paragraph({
                    text: '教学建议：',
                    spacing: { after: 100 }
                }),
                new Paragraph({
                    text: content.teachingAdvice,
                    spacing: { after: 400 }
                }),

                // 课时内容
                new Paragraph({
                    text: '课时详细内容',
                    heading: HeadingLevel.HEADING_1,
                    spacing: { before: 400, after: 200 }
                }),

                ...content.lessons.flatMap((lesson, index) => [
                    // 课时标题
                    new Paragraph({
                        text: `第${index + 1}课时：${lesson.title}`,
                        heading: HeadingLevel.HEADING_2,
                        spacing: { before: 400, after: 200 }
                    }),
                    // 课时信息表格
                    createLessonTable(lesson),
                ])
            ]
        }]
    })

    const blob = await Packer.toBlob(doc)
    saveAs(blob, `${courseName}-教学内容设计.docx`)
}

/**
 * 创建课时内容表格
 */
function createLessonTable(lesson: LessonContent): Table {
    return new Table({
        width: {
            size: 100,
            type: WidthType.PERCENTAGE
        },
        rows: [
            // 建议课时
            new TableRow({
                children: [
                    new TableCell({
                        children: [new Paragraph('建议课时')],
                        width: {
                            size: 20,
                            type: WidthType.PERCENTAGE
                        }
                    }),
                    new TableCell({
                        children: [new Paragraph(lesson.suggestedHours.toString())],
                    })
                ]
            }),
            // 知识点讲解
            new TableRow({
                children: [
                    new TableCell({
                        children: [new Paragraph('知识点讲解')],
                        width: {
                            size: 20,
                            type: WidthType.PERCENTAGE
                        }
                    }),
                    new TableCell({
                        children: [new Paragraph(lesson.content)]
                    })
                ]
            }),
            // 实训练习
            new TableRow({
                children: [
                    new TableCell({
                        children: [new Paragraph('实训练习')],
                        width: {
                            size: 20,
                            type: WidthType.PERCENTAGE
                        }
                    }),
                    new TableCell({
                        children: [new Paragraph(lesson.practiceContent)]
                    })
                ]
            }),
            // 教学指导
            new TableRow({
                children: [
                    new TableCell({
                        children: [new Paragraph('教学指导')],
                        width: {
                            size: 20,
                            type: WidthType.PERCENTAGE
                        }
                    }),
                    new TableCell({
                        children: [new Paragraph(lesson.teachingGuidance)]
                    })
                ]
            }),
            // 知识点来源
            new TableRow({
                children: [
                    new TableCell({
                        children: [new Paragraph('知识点来源')],
                        width: {
                            size: 20,
                            type: WidthType.PERCENTAGE
                        }
                    }),
                    new TableCell({
                        children: [new Paragraph(lesson.knowledgeSources.join('\n'))]
                    })
                ]
            })
        ]
    })
}

/**
 * 导出为JSON文件
 */
export function exportToJson(content: TeachingContentResponse, courseName: string) {
    const blob = new Blob([JSON.stringify(content, null, 2)], { type: 'application/json' })
    saveAs(blob, `${courseName}-教学内容设计.json`)
}

/**
 * 导出为Markdown文件
 */
export function exportToMarkdown(content: TeachingContentResponse, courseName: string) {
    const markdown = `# ${courseName} - 教学内容设计

## 总体教学安排

- 总课时：${content.totalHours}

### 时间分配建议
${content.timeDistribution}

### 教学建议
${content.teachingAdvice}

## 课时详细内容

${content.lessons.map((lesson, index) => `
### 第${index + 1}课时：${lesson.title}

- 建议课时：${lesson.suggestedHours}

#### 知识点讲解
${lesson.content}

#### 实训练习
${lesson.practiceContent}

#### 教学指导
${lesson.teachingGuidance}

#### 知识点来源
${lesson.knowledgeSources.map(source => `- ${source}`).join('\n')}
`).join('\n')}
`

    const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' })
    saveAs(blob, `${courseName}-教学内容设计.md`)
}
