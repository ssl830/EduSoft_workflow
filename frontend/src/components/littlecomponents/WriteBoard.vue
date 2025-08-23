<template>
  <div>
    <!-- 工具栏 -->
    <div class="wb-toolbar">
      <button type="button" class="wb-btn" @click="insertFormat('**','**')" title="粗体"><b>B</b></button>
      <button type="button" class="wb-btn" @click="insertFormat('*','*')" title="斜体"><i>I</i></button>
      <button type="button" class="wb-btn" @click="insertFormat('~~','~~')" title="删除线"><span style="text-decoration:line-through;">S</span></button>
      <span class="wb-divider"></span>
      <button type="button" class="wb-btn" @click="insertFormat('`','`')" title="行内代码"><span style="font-family:monospace;">&lt;/&gt;</span></button>
      <button type="button" class="wb-btn" @click="insertFormat('\n````\n','\n````\n')" title="代码块"><span style="font-family:monospace;">[code]</span></button>
      <span class="wb-divider"></span>
      <button type="button" class="wb-btn" @click="toggleEmojiPicker" title="表情">😊</button>
      <span style="font-size:12px;color:#888;margin-left:8px;">支持Markdown格式</span>
    </div>
    <!-- 表情选择器 -->
    <div v-if="showEmojiPicker" class="wb-emoji-picker">
      <span v-for="emoji in emojis" :key="emoji" class="wb-emoji" @click="insertEmoji(emoji)">{{ emoji }}</span>
    </div>
    <textarea
      ref="textareaRef"
      v-model="localContent"
      :placeholder="placeholder"
      :maxlength="maxLength"
      rows="8"
      class="wb-textarea"
      @focus="handleFocus"
      @blur="handleBlur"
    ></textarea>
    <div class="wb-count">
      {{ localContent.length }} / {{ maxLength }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容...' },
  maxLength: { type: Number, default: 3000 }
})
const emit = defineEmits(['update:modelValue', 'focus', 'blur'])

const localContent = ref(props.modelValue)
const textareaRef = ref<HTMLTextAreaElement>()
const showEmojiPicker = ref(false)
const emojis = [
  '😀','😃','😄','😁','😆','😅','😂','🤣','😊','😇','🙂','🙃','😉','😍','😘','😜','🤔','😎','😭','😡','👍','👎','👏','🙏','🎉','💯','🔥','🌟','🥳','🤩','😏','😋','😝','😤','😱','😳','😅','🥲','😇','😚','😙','😗','😐','😶','😑','😬','😪','😴','😵','🤐','🤑','🤠','😈','👻','💀','🤖','🎃','😺','😸','😹','😻','😼','😽','🙀','😿','😾'
]

watch(() => props.modelValue, (val) => {
  if (val !== localContent.value) localContent.value = val
})
watch(localContent, (val) => {
  emit('update:modelValue', val)
})

function handleFocus() { emit('focus') }
function handleBlur() { emit('blur') }

function insertFormat(start:string, end:string) {
  if (!textareaRef.value) return
  const textarea = textareaRef.value
  const startPos = textarea.selectionStart
  const endPos = textarea.selectionEnd
  const selected = localContent.value.substring(startPos, endPos)
  let insertText = start + selected + end
  // 代码块特殊处理
  if (start.includes('``')) {
    insertText = start + (selected || '\n代码内容\n') + end
  }
  localContent.value = localContent.value.substring(0, startPos) + insertText + localContent.value.substring(endPos)
  nextTick(() => {
    textarea.focus()
    if (selected) {
      textarea.setSelectionRange(startPos + start.length, startPos + start.length + selected.length)
    } else {
      textarea.setSelectionRange(startPos + start.length, startPos + start.length)
    }
  })
}
function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
}
function insertEmoji(emoji:string) {
  if (!textareaRef.value) return
  const textarea = textareaRef.value
  const start = textarea.selectionStart
  localContent.value = localContent.value.substring(0, start) + emoji + localContent.value.substring(start)
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + emoji.length, start + emoji.length)
  })
  showEmojiPicker.value = false
}
</script>

<style scoped>
.wb-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  align-items: center;
  flex-wrap: wrap;
  background: #f7fafd;
  border-radius: 8px;
  padding: 6px 10px;
  box-shadow: 0 1px 4px rgba(44,110,207,0.04);
}
.wb-btn {
  background: #fff;
  border: 1px solid #e3e8ee;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, border 0.18s;
  outline: none;
  min-width: 32px;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}
.wb-btn:hover, .wb-btn:focus {
  background: #e3f2fd;
  border-color: #90caf9;
  box-shadow: 0 2px 8px rgba(44,110,207,0.08);
}
.wb-divider {
  width: 1px;
  height: 22px;
  background: #e3e8ee;
  margin: 0 4px;
  border-radius: 2px;
}
.wb-emoji-picker {
  background: #fff;
  border: 1px solid #e3e8ee;
  border-radius: 8px;
  padding: 6px 8px;
  margin-bottom: 6px;
  box-shadow: 0 2px 8px rgba(44,110,207,0.08);
  max-width: 420px;
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}
.wb-emoji {
  cursor: pointer;
  font-size: 20px;
  padding: 2px 4px;
  border-radius: 4px;
  transition: background 0.15s;
}
.wb-emoji:hover {
  background: #e3f2fd;
}
.wb-textarea {
  width: 100%;
  min-height: 120px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 15px;
  resize: vertical;
  box-sizing: border-box;
  transition: border 0.18s, box-shadow 0.18s;
}
.wb-textarea:focus {
  border-color: #90caf9;
  box-shadow: 0 0 0 2px #e3f2fd;
  outline: none;
}
.wb-count {
  text-align: right;
  font-size: 12px;
  color: #888;
  margin-top: 2px;
}
</style>
