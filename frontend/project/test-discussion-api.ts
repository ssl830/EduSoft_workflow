// 讨论API集成验证脚本
// 这个文件可以用于测试新的讨论API是否正常工作

import discussionApi from '@/src/api/discussion.ts';

// 测试用例
const testCases = {
  // 测试获取课程讨论
  async testGetDiscussionsByCourse() {
    try {
      console.log('测试获取课程讨论...');
      const response = await discussionApi.getDiscussionsByCourse(1);
      console.log('✅ 获取课程讨论成功:', response.data);
      return true;
    } catch (error) {
      console.error('❌ 获取课程讨论失败:', error);
      return false;
    }
  },

  // 测试获取班级讨论
  async testGetDiscussionsByClass() {
    try {
      console.log('测试获取班级讨论...');
      const response = await discussionApi.getDiscussionsByClass(1);
      console.log('✅ 获取班级讨论成功:', response.data);
      return true;
    } catch (error) {
      console.error('❌ 获取班级讨论失败:', error);
      return false;
    }
  },

  // 测试创建讨论
  async testCreateDiscussion() {
    try {
      console.log('测试创建讨论...');
      const response = await discussionApi.createDiscussion(1, 1, {
        title: '测试讨论标题',
        content: '这是一个测试讨论的内容'
      });
      console.log('✅ 创建讨论成功:', response.data);
      return response.data.id;
    } catch (error) {
      console.error('❌ 创建讨论失败:', error);
      return null;
    }
  },

  // 测试获取讨论详情
  async testGetDiscussionDetail(discussionId: number) {
    try {
      console.log('测试获取讨论详情...');
      const response = await discussionApi.getDiscussionDetail(discussionId);
      console.log('✅ 获取讨论详情成功:', response.data);
      return true;
    } catch (error) {
      console.error('❌ 获取讨论详情失败:', error);
      return false;
    }
  },

  // 测试置顶功能
  async testPinDiscussion(discussionId: number) {
    try {
      console.log('测试置顶功能...');
      await discussionApi.updateDiscussionPinStatus(discussionId, true);
      console.log('✅ 置顶讨论成功');
      
      // 取消置顶
      await discussionApi.updateDiscussionPinStatus(discussionId, false);
      console.log('✅ 取消置顶成功');
      return true;
    } catch (error) {
      console.error('❌ 置顶功能测试失败:', error);
      return false;
    }
  },

  // 测试关闭功能
  async testCloseDiscussion(discussionId: number) {
    try {
      console.log('测试关闭功能...');
      await discussionApi.updateDiscussionCloseStatus(discussionId, true);
      console.log('✅ 关闭讨论成功');
      
      // 重新开启
      await discussionApi.updateDiscussionCloseStatus(discussionId, false);
      console.log('✅ 开启讨论成功');
      return true;
    } catch (error) {
      console.error('❌ 关闭功能测试失败:', error);
      return false;
    }
  },

  // 测试回复功能
  async testCreateReply(discussionId: number) {
    try {
      console.log('测试创建回复...');
      const response = await discussionApi.createReply(discussionId, {
        content: '这是一个测试回复'
      });
      console.log('✅ 创建回复成功:', response.data);
      return response.data.id;
    } catch (error) {
      console.error('❌ 创建回复失败:', error);
      return null;
    }
  },

  // 测试获取回复列表
  async testGetReplies(discussionId: number) {
    try {
      console.log('测试获取回复列表...');
      const response = await discussionApi.getTopLevelReplies(discussionId);
      console.log('✅ 获取回复列表成功:', response.data);
      return true;
    } catch (error) {
      console.error('❌ 获取回复列表失败:', error);
      return false;
    }
  },

  // 测试删除讨论
  async testDeleteDiscussion(discussionId: number) {
    try {
      console.log('测试删除讨论...');
      await discussionApi.deleteDiscussion(discussionId);
      console.log('✅ 删除讨论成功');
      return true;
    } catch (error) {
      console.error('❌ 删除讨论失败:', error);
      return false;
    }
  }
};

// 运行所有测试
export async function runAllTests() {
  console.log('🚀 开始运行讨论API集成测试...\n');
  
  let passedTests = 0;
  let totalTests = 0;
  
  // 测试获取功能
  totalTests++;
  if (await testCases.testGetDiscussionsByCourse()) passedTests++;
  
  totalTests++;
  if (await testCases.testGetDiscussionsByClass()) passedTests++;
  
  // 测试创建和其他功能
  const discussionId = await testCases.testCreateDiscussion();
  totalTests++;
  
  if (discussionId) {
    passedTests++;
    
    // 测试获取详情
    totalTests++;
    if (await testCases.testGetDiscussionDetail(discussionId)) passedTests++;
    
    // 测试置顶功能
    totalTests++;
    if (await testCases.testPinDiscussion(discussionId)) passedTests++;
    
    // 测试关闭功能
    totalTests++;
    if (await testCases.testCloseDiscussion(discussionId)) passedTests++;
    
    // 测试回复功能
    const replyId = await testCases.testCreateReply(discussionId);
    totalTests++;
    if (replyId) passedTests++;
    
    // 测试获取回复
    totalTests++;
    if (await testCases.testGetReplies(discussionId)) passedTests++;
    
    // 最后删除测试数据
    totalTests++;
    if (await testCases.testDeleteDiscussion(discussionId)) passedTests++;
  }
  
  console.log(`\n📊 测试完成: ${passedTests}/${totalTests} 个测试通过`);
  
  if (passedTests === totalTests) {
    console.log('🎉 所有测试都通过了！API集成成功！');
  } else {
    console.log('⚠️  部分测试失败，请检查API配置和后端服务');
  }
  
  return { passed: passedTests, total: totalTests };
}

// 单独测试函数导出
export default testCases;
