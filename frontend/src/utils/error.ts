/**
 * 错误处理工具函数
 */

/**
 * 调试警告输出
 * @param err 错误对象
 */
export function debugWarn(err: Error): void {
  if (process.env.NODE_ENV !== 'production') {
    console.warn('[CourtLink Warning]:', err);
    
    // 如果是Axios错误，输出更详细的信息
    if (err.name === 'AxiosError') {
      const axiosError = err as any;
      console.warn('请求详情:', {
        config: axiosError.config,
        response: axiosError.response?.data,
        status: axiosError.response?.status
      });
    }
  }
} 