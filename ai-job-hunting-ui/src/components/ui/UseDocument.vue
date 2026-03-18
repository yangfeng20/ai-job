<template>
    <div class="markdown-container">
        <MarkdownComponent />
    </div>
</template>

<script setup lang="ts">
import MarkdownComponent  from '../../assets/UseDocument.md';


</script>

<style scoped>
.markdown-container {
    padding: 16px;
    background-color: #f9f9f9;
    border-radius: 8px;
    font-family: Arial, sans-serif;
    line-height: 1.6;
    color: #333;
}

.markdown-container h1 {
    font-size: 2em;
    margin-bottom: 0.5em;
    color: #222;
}

.markdown-container h2 {
    font-size: 1.5em;
    margin-bottom: 0.5em;
    color: #333;
}

.markdown-container h3 {
    font-size: 1.25em;
    margin-bottom: 0.5em;
    color: #444;
}

.markdown-container p {
    margin: 0.5em 0;
}

.markdown-container ul {
    padding-left: 20px;
    list-style-type: disc;
}

.markdown-container ol {
    padding-left: 20px;
    list-style-type: decimal;
}

.markdown-container li {
    margin: 0.5em 0;
}

.markdown-container code {
    background-color: #f4f4f4;
    padding: 0.2em 0.4em;
    border-radius: 4px;
    font-family: 'Courier New', Courier, monospace;
}

.markdown-container pre {
    background-color: #f4f4f4;
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
}

.markdown-container blockquote {
    margin: 1em 0;
    padding: 0.5em 1em;
    background-color: #f9f9f9;
    border-left: 4px solid #ccc;
    color: #666;
    font-style: italic;
}
</style>