import { mount } from '@vue/test-utils'

import App from '@/App'
import Todo from '@/components/Todo'
import router from '@/router.js'

describe('router.js', () => {
  describe('/todo にアクセスした場合', () => {
    it('Todoコンポーネントが表示される', () => {
      const wrapper = mount(App, { router, Todo })
      expect(wrapper.find(Todo).exists()).toEqual(false)

      router.push('/todo')

      expect(wrapper.find(Todo).exists()).toEqual(true)
    })
  })
})
