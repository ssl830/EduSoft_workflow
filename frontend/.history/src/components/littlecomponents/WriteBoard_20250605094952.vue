<template>
  <div class="q-pa-md q-gutter-sm">
    <q-editor
      v-model="editorContent"
      :dense="$q.screen.lt.md"      :toolbar="[
        ['bold', 'italic', 'underline', 'strike'],
        [
          {
            label: $q.lang.editor.fontSize,
            icon: $q.iconSet.editor.fontSize,
            fixedLabel: true,
            fixedIcon: true,
            list: 'no-icons',
            options: [
              'size-3',
              'size-4',
              'size-5',
              'size-6'
            ]
          },
          {
            label: $q.lang.editor.align,
            icon: $q.iconSet.editor.align,
            fixedLabel: true,
            list: 'only-icons',
            options: ['left', 'center', 'right']
          }
        ],
        ['quote', 'unordered', 'ordered'],
        ['link', 'hr'],
        ['undo', 'redo']
      ]"
      :fonts="{
        arial: 'Arial',
        arial_black: 'Arial Black',
        comic_sans: 'Comic Sans MS',
        courier_new: 'Courier New',
        impact: 'Impact',
        lucida_grande: 'Lucida Grande',
        times_new_roman: 'Times New Roman',
        verdana: 'Verdana'
      }"
    />
    </div>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'WriteBoard',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editorContent = ref(props.modelValue || '');
    
    // 监听父组件传入的值变化
    watch(() => props.modelValue, (newVal) => {
      if (newVal !== editorContent.value) {
        editorContent.value = newVal;
      }
    });
    
    // 监听编辑器内容变化，向父组件发送更新
    watch(editorContent, (newVal) => {
      emit('update:modelValue', newVal);
    });
    
    return {
      editorContent
    }
  }
}
</script>
