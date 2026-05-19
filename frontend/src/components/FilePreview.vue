<template>
	<div class="file-preview-container">
		<template v-if="isImage">
			<el-image
				style="width: 100px; height: 100px"
				:src="url"
				:preview-src-list="[url]"
				:initial-index="0"
				fit="cover"
				preview-teleported
			/>
		</template>
		<template v-else-if="isPdf">
			<el-button type="primary" link @click="pdfVisible = true">
				<el-icon><Document /></el-icon> 查看 PDF
			</el-button>

			<el-dialog v-model="pdfVisible" title="PDF 预览" width="80%" destroy-on-close append-to-body>
				<iframe :src="url" width="100%" height="600px" frameborder="0"></iframe>
			</el-dialog>
		</template>
		<template v-else>
			<el-link :href="url" target="_blank" type="primary">直接下载</el-link>
		</template>
	</div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
	url: {
		type: String,
		required: true,
	},
})

const pdfVisible = ref(false)

const isImage = computed(() => {
	if (!props.url) return false
	const ext = props.url.split('.').pop().toLowerCase()
	return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)
})

const isPdf = computed(() => {
	if (!props.url) return false
	const ext = props.url.split('.').pop().toLowerCase()
	return ext === 'pdf'
})
</script>

<style scoped>
.file-preview-container {
	display: inline-block;
}
</style>
