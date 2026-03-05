---
name: doc-standardizer
description: Standardizes requirement documentation into a specific format with fixed personas and task structures. Use this skill when the user wants to normalize a document or generate a new requirement doc following the "Kuaishou/ADX" template style.
---

# Document Standardizer Skill

This skill helps normalize requirement documentation into a standardized format with specific personas and workflows, particularly for the ADX system context.

## Standard Template Structure

The output document MUST follow this structure:

1.  **# 重点文件** (Key Files)
    *   Extract file paths and method names from the input.
    *   If none provided, leave this section empty or omit if irrelevant.

2.  **# 任务背景** (Task Background)
    *   Include the core requirements, background, and solution proposals from the input.
    *   Structure into subsections like `### 一、需求背景` and `### 二、需求方案` if possible.

3.  **# 我的情况** (My Situation - Fixed Persona)
    *   **Content**: `我是一个刚刚入职的只有一年开发经验的java后端开发，对当前adx业务系统不了解，且很多术语不清楚，请你详细讲解代码的时候，遇到各种术语都通过补充来详细的介绍`

4.  **# 你的情况** (Your Situation - Fixed Persona)
    *   **Content**: `你是有多年java开发经验的程序员，且对当前adx业务系统非常熟悉，你耐心且细心的写详细的文档，且编码能力极强`

5.  **# 你的任务** (Your Task - To be generated)
    *   **Generation Logic**: Based on the user's input, identify the goal.
    *   **Standard Definition (Spec)**:
        *   Task 1: Generate a design document (System Design / Technical Design).
        *   Task 2: Implement the code/requirements.
    *   **Draft Content Example**:
        ```markdown
        ① 在 [Project/Path] 中生成代码设计文档
        ② 完成上述需求
        ```
    *   **IMPORTANT**: You must ask the user to confirm this section if the input is ambiguous.

6.  **# 注意** (Attention - Fixed Constraint)
    *   **Content**: `请先生成设计文档，在我确定设计文档无问题后生成代码`

## Workflow

1.  **Input Analysis**: Read the user's provided text or file.
2.  **Extraction**: Identify the "Key Files", "Background", and "Solution" info.
3.  **Drafting**: Assemble the document using the Standard Template.
4.  **Task Definition**: Formulate the `# 你的任务` section. Defaults to "Design Doc -> Implementation" flow.
5.  **Output**: effective markdown document.

## Usage Example

**User**: "Help me standardize this requirement doc..."

**Agent Action**:
1.  Apply the template.
2.  Inject the fixed personas (`# 我的情况`, `# 你的情况`).
3.  Inject the fixed constraint (`# 注意`).
4.  Format the user's content into `# 任务背景`.
5.  Propose the `# 你的任务` section.
