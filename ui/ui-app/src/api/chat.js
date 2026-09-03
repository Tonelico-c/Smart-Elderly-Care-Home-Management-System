import request from "@/utils/request.js";
import { useTokenStore } from "@/store/token.js";

const chatApi = {
  //普通对话（一次性返回完整回复）
  chat(message) {
    return request.post("/chat/chat", null, { params: { message } })
  },
  //流式对话（SSE），逐段回调 onMessage(片段)，结束后回调 onDone()
  chatStream(message, { onMessage, onDone, onError }) {
    const tokenStore = useTokenStore()
    //后端接口为/app/chat/chatStream，request的baseURL是/api/app，这里保持一致走vite代理
    return fetch('/api/app/chat/chatStream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': tokenStore.token || ''
      },
      body: 'message=' + encodeURIComponent(message)
    }).then(response => {
      if (!response.ok) {
        throw new Error('请求失败')
      }
      //逐块读取SSE流并解析出data:后面的内容
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      //解析一个SSE事件：事件内可能有多行data:，按SSE规范用换行拼接成一个片段
      const handleEvent = (event) => {
        const dataLines = []
        for (const line of event.split('\n')) {
          if (line.startsWith('data:')) {
            //去掉"data:"前缀，兼容"data: xxx"带空格的格式
            let data = line.slice(5)
            if (data.startsWith(' ')) {
              data = data.slice(1)
            }
            dataLines.push(data)
          }
        }
        if (dataLines.length === 0) {
          return false
        }
        const data = dataLines.join('\n')
        //后端约定的流结束标记
        if (data === '[END]') {
          return true
        }
        onMessage && onMessage(data)
        return false
      }

      const read = () => {
        return reader.read().then(({ done, value }) => {
          if (done) {
            onDone && onDone()
            return
          }
          buffer += decoder.decode(value, { stream: true })
          //SSE事件以空行分隔，每个事件形如 data:xxx
          const events = buffer.split('\n\n')
          //最后一段可能不完整，留在缓冲区下次拼接
          buffer = events.pop() || ''
          for (const event of events) {
            if (handleEvent(event)) {
              reader.cancel()
              onDone && onDone()
              return
            }
          }
          return read()
        })
      }
      return read()
    }).catch(error => {
      onError && onError(error)
    })
  }
}

export default chatApi
