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
                        .withCompensation(orderService.reject, CreateOrderSagaState::makeRejectOrderCommand)
                        .step()

                                 //校验订单
                        .invokeParticipant(consumerService.validateOrder, CreateOrderSagaState::makeValidateOrderByConsumerCommand)
                        .step()

                                 //创建工单
                        .invokeParticipant(kitchenService.create, CreateOrderSagaState::makeCreateTicketCommand)
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
