import { mount } from '@vue/test-utils'

import Todo from '@/components/Todo'
import TodoService from '@/services/TodoService'

describe('Todo.vue', () => {

  describe('メソッドの検証', () => {
    describe('getAll() service 処理が成功する場合', () => {
      it('返却値がセットされる', async () => {
        const wrapper = mount(Todo)

        const spy = jest.spyOn(TodoService.prototype, 'getAll').mockImplementation(() => Promise.resolve({ data: 1 }))

        wrapper.vm.getAll()

        await wrapper.vm.$nextTick()

        expect(wrapper.vm.todos).toEqual(1)
        expect(wrapper.vm.message).toEqual('')

        spy.mockRestore()
      })
    })

    describe('getAll() service 処理が失敗する場合', () => {
      it('メッセージがセットされる', async () => {
        const wrapper = mount(Todo)

        const spy = jest.spyOn(TodoService.prototype, 'getAll').mockImplementation(() => Promise.reject({ data: 1 }))

        wrapper.vm.getAll()

        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()
          
        expect(wrapper.vm.todos).toHaveLength(0)
        expect(wrapper.vm.message).toEqual('error')
        
        spy.mockRestore()
      })
    })
  })

  describe('コンポーネントの振る舞いを検証', () => {
    describe('初期表示の場合', () => {
      it('TODOリストは表示されない', () => {
        const wrapper = mount(Todo)

        expect(wrapper.find('.table tbody').element.hasChildNodes()).toEqual(false)
      })
    })

    describe('ボタンをクリックした場合', () => {
      it('TODOリストが表示される', async () => {
        const todo1 = {
          todoId: 1,
          title: 'title1',
          status: 'status1',
          description: 'desc1'
        }
        const todo2 = {
          todoId: 2,
          title: 'title2',
          status: 'status2',
          description: 'desc2'
        }

        const wrapper = mount(Todo)
        const spy = jest.spyOn(TodoService.prototype, 'getAll').mockImplementation(() => Promise.resolve({ data: [todo1, todo2] }))

        wrapper.find('form').trigger('submit.prevent')

        await wrapper.vm.$nextTick()
        
        expect(wrapper.findAll('.table tbody tr')).toHaveLength(2)
        expect(wrapper.findAll('.table tbody .index').at(0).element.textContent).toEqual(String(todo1.todoId))
        expect(wrapper.findAll('.table tbody .title input').at(0).element.value).toEqual(todo1.title)
        expect(wrapper.findAll('.table tbody .status input').at(0).element.value).toEqual(todo1.status)
        expect(wrapper.findAll('.table tbody .desc textarea').at(0).element.value).toEqual(todo1.description)
        expect(wrapper.findAll('.table tbody .index').at(1).element.textContent).toEqual(String(todo2.todoId))
        expect(wrapper.findAll('.table tbody .title input').at(1).element.value).toEqual(todo2.title)
        expect(wrapper.findAll('.table tbody .status input').at(1).element.value).toEqual(todo2.status)
        expect(wrapper.findAll('.table tbody .desc textarea').at(1).element.value).toEqual(todo2.description)

        spy.mockRestore()
      })
    })
  })
})
