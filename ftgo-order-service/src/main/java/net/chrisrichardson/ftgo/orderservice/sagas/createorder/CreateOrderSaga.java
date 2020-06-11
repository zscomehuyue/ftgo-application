package net.chrisrichardson.ftgo.orderservice.sagas.createorder;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;
import net.chrisrichardson.ftgo.orderservice.sagaparticipants.*;
import net.chrisrichardson.ftgo.kitchenservice.api.CreateTicketReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateOrderSaga implements SimpleSaga<CreateOrderSagaState> {


    private Logger logger = LoggerFactory.getLogger(getClass());

    private SagaDefinition<CreateOrderSagaState> sagaDefinition;

    /**
     * FIXME 创建订单流程图
     * FIXME 1.订单服务
     * FIXME 2.工单服务
     * FIXME 3.账户服务
     * FIXME 4.消费者服务
     * FIXME 把创建订单的流程图，使用状态图的方式，来分段表示出来；
     */
    public CreateOrderSaga(OrderServiceProxy orderService,
                           ConsumerServiceProxy consumerService,
                           KitchenServiceProxy kitchenService,
                           AccountingServiceProxy accountingService) {
        this.sagaDefinition =
                step()

                        //拒绝订单
                        //TODO  创建订单为何包含拒绝的场景？？
                        //TODO  订单创建流程如何改成如下的步骤？
                        //TODO  如何映射成，状态机的相关方法：action 、withCompensation、invokeParticipant、onReply
                        //TODO  状态需要在哪些实体上增加；

//                        saga中间协调者

                        //补偿事务，当取消命令时，撤销该订单，可以放在前面，触发后被调用；
                        //当orderSerivce发生拒绝时，要发送拒绝命令；相关方，需要知道该订单是否成功还是拒绝；
                        .withCompensation(orderService.reject, CreateOrderSagaState::makeRejectOrderCommand)
                        .step()


                        //校验结果，saga如何处理的？
                        //校验订单，消费服务参与者，参与了，校验；sagaManager发送给消费参数者命令，
                        .invokeParticipant(consumerService.validateOrder, CreateOrderSagaState::makeValidateOrderByConsumerCommand)
                        .step()

                        //创建工单
                        .invokeParticipant(kitchenService.create, CreateOrderSagaState::makeCreateTicketCommand)

                        //处理ticket create返回的结果；
                        .onReply(CreateTicketReply.class, CreateOrderSagaState::handleCreateTicketReply)

                        .withCompensation(kitchenService.cancel, CreateOrderSagaState::makeCancelCreateTicketCommand)
                        .step()

                        //账户授权
                        .invokeParticipant(accountingService.authorize, CreateOrderSagaState::makeAuthorizeCommand)
                        .step()

                        //工单确认创建
                        .invokeParticipant(kitchenService.confirmCreate, CreateOrderSagaState::makeConfirmCreateTicketCommand)
                        .step()

                        //订单批准
                        .invokeParticipant(orderService.approve, CreateOrderSagaState::makeApproveOrderCommand)
                        .build();

    }


    @Override
    public SagaDefinition<CreateOrderSagaState> getSagaDefinition() {
        return sagaDefinition;
    }


}
