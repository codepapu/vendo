# How to Use Build Prompts

## Overview

The `BUILD_PROMPTS.md` file contains 50 step-by-step prompts to build the complete Vendo platform. Each prompt is designed to be copied and pasted directly to guide implementation of a specific feature.

## Usage Instructions

### Step 1: Open BUILD_PROMPTS.md
```bash
# View the prompts file
cat BUILD_PROMPTS.md
# Or open in your editor
```

### Step 2: Copy a Prompt
1. Find the prompt you want to implement (start with Prompt 1)
2. Copy the entire prompt (all 5 lines)
3. Paste it into your conversation with the AI assistant

### Step 3: Implementation
The AI will:
- Create the necessary files
- Implement the code
- Follow the specifications in the prompt
- Ensure integration with existing code

### Step 4: Review & Test
1. Review the generated code
2. Test the implementation
3. Fix any issues
4. Move to the next prompt

## Recommended Order

### Week 1-2: Foundation
- Prompts 1-6: Database setup and entities

### Week 3-4: Backend B2B
- Prompts 7-11: B2B services and controllers

### Week 5-6: Backend Retailer
- Prompts 12-15: Retailer services and controllers

### Week 7: Enhanced Orders
- Prompts 16-18: Multi-type order processing

### Week 8-10: Frontend B2B
- Prompts 19-26: B2B frontend features

### Week 11-13: Frontend Retailer
- Prompts 27-34: Retailer frontend features

### Week 14: Cart & Checkout
- Prompts 35-37: Enhanced checkout flows

### Week 15-16: Admin & Mobile
- Prompts 38-42: Admin and mobile features

### Week 17-18: Testing & Quality
- Prompts 43-45: Comprehensive testing

### Week 19-20: Optimization
- Prompts 46-48: Performance optimization

### Week 21-22: Deployment
- Prompts 49-50: Documentation and deployment

## Tips for Success

### 1. One Prompt at a Time
- Don't skip prompts
- Complete each one fully before moving on
- Test after each implementation

### 2. Review Generated Code
- Check that it follows your coding standards
- Ensure proper error handling
- Verify integration points

### 3. Test Thoroughly
- Write unit tests
- Test integration points
- Test edge cases

### 4. Document as You Go
- Add comments to complex code
- Update API documentation
- Note any deviations from the plan

### 5. Commit Frequently
- Commit after each prompt completion
- Use descriptive commit messages
- Tag major milestones

## Example Workflow

```bash
# 1. Copy Prompt 1 from BUILD_PROMPTS.md
# 2. Paste to AI assistant
# 3. Review generated migration files
# 4. Test database migration
# 5. Commit changes
git add .
git commit -m "feat: Add B2B database tables (Prompt 1)"

# 6. Move to Prompt 2
# Repeat process...
```

## Customization

You can modify prompts to:
- Add specific requirements
- Change implementation approach
- Include additional features
- Adjust to your coding standards

## Troubleshooting

### If a prompt doesn't work:
1. Check that prerequisites are met
2. Review error messages
3. Adjust the prompt with more context
4. Break it into smaller prompts

### If code doesn't integrate:
1. Check existing code structure
2. Review dependencies
3. Update imports and references
4. Test integration points

## Progress Tracking

Keep track of completed prompts:

```markdown
## Completed Prompts
- [x] Prompt 1: B2B Database Tables
- [x] Prompt 2: Retailer Database Tables
- [ ] Prompt 3: Enhanced Order Tables
- [ ] ...
```

## Next Steps

1. Open `BUILD_PROMPTS.md`
2. Start with Prompt 1
3. Copy and paste to begin implementation
4. Follow the recommended order
5. Track your progress

Happy building! 🚀
