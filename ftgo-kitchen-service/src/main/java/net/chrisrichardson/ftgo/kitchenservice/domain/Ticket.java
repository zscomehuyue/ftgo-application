package net.chrisrichardson.ftgo.kitchenservice.domain;

import io.eventuate.tram.events.aggregates.ResultWithDomainEvents;
import net.chrisrichardson.ftgo.common.NotYetImplementedException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.kitchenservice.api.TicketDetails;
import net.chrisrichardson.ftgo.kitchenservice.api.TicketLineItem;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Entity
@Table(name = "tickets")
@Access(AccessType.FIELD)
public class Ticket {

    @Id
    private Long id;

    //FIXME 通过状态，来把数据分不同的阶段，可以规避数据出现问题；
    @Enumerated(EnumType.STRING)
    private TicketState state;

    private TicketState previousState;

    private Long restaurantId;

    @ElementCollection
    @CollectionTable(name = "ticket_line_items")
    private List<TicketLineItem> lineItems;

    private LocalDateTime readyBy;
    private LocalDateTime acceptTime;
    private LocalDateTime preparingTime;
    private LocalDateTime pickedUpTime;
    private LocalDateTime readyForPickupTime;

    public static ResultWithDomainEvents<Ticket, TicketDomainEvent> create(long restaurantId, Long id, TicketDetails details) {
        return new ResultWithDomainEvents<>(new Ticket(restaurantId, id, details));
    }

    private Ticket() {
    }

    public Ticket(long restaurantId, Long id, TicketDetails details) {
        this.restaurantId = restaurantId;
        this.id = id;
        this.state = TicketState.CREATE_PENDING;
        this.lineItems = details.getLineItems();
    }

    public List<TicketDomainEvent> confirmCreate() {
        switch (state) {
            case CREATE_PENDING:
                state = TicketState.AWAITING_ACCEPTANCE;
                return singletonList(new TicketCreatedEvent(id, new TicketDetails()));
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    public List<TicketDomainEvent> cancelCreate() {
        throw new NotYetImplementedException();
    }


    public List<TicketDomainEvent> accept(LocalDateTime readyBy) {
        switch (state) {
            case AWAITING_ACCEPTANCE:
                // Verify that readyBy is in the futurestate = TicketState.ACCEPTED;
                this.acceptTime = LocalDateTime.now();
                if (!acceptTime.isBefore(readyBy))
                    throw new IllegalArgumentException("readyBy is not in the future");
                this.readyBy = readyBy;
                return singletonList(new TicketAcceptedEvent(readyBy));
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    // TODO reject()

    // TODO cancel()

    public List<TicketDomainEvent> preparing() {
        switch (state) {
            case ACCEPTED:
                this.state = TicketState.PREPARING;
                this.preparingTime = LocalDateTime.now();
                return singletonList(new TicketPreparationStartedEvent());
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    public List<TicketDomainEvent> readyForPickup() {
        switch (state) {
            case PREPARING:
                this.state = TicketState.READY_FOR_PICKUP;
                this.readyForPickupTime = LocalDateTime.now();
                return singletonList(new TicketPreparationCompletedEvent());
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    public List<TicketDomainEvent> pickedUp() {
        switch (state) {
            case READY_FOR_PICKUP:
                this.state = TicketState.PICKED_UP;
                this.pickedUpTime = LocalDateTime.now();
                return singletonList(new TicketPickedUpEvent());
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    public void changeLineItemQuantity() {
        switch (state) {
            case AWAITING_ACCEPTANCE:
                // TODO
                break;
            case PREPARING:
                // TODO - too late
                break;
            default:
                throw new UnsupportedStateTransitionException(state);
        }

    }

    public List<TicketDomainEvent> cancel() {
        switch (state) {
            case AWAITING_ACCEPTANCE:
            case ACCEPTED:
                this.previousState = state;
                this.state = TicketState.CANCEL_PENDING;
                return emptyList();
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    public Long getId() {
        return id;
    }

    public List<TicketDomainEvent> confirmCancel() {
        switch (state) {
            case CANCEL_PENDING:
                this.state = TicketState.CANCELLED;
                return singletonList(new TicketCancelled());
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    public List<TicketDomainEvent> undoCancel() {
        switch (state) {
            case CANCEL_PENDING:
                this.state = this.previousState;
                return emptyList();
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    /**
     * 修订订单
     */
    public List<TicketDomainEvent> beginReviseOrder(Map<String, Integer> revisedLineItemQuantities) {
        switch (state) {
            case AWAITING_ACCEPTANCE:
            case ACCEPTED:
                this.previousState = state;
                this.state = TicketState.REVISION_PENDING;
                return emptyList();
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    /**
     * 撤销修改订单
     */
    public List<TicketDomainEvent> undoBeginReviseOrder() {
        switch (state) {
            case REVISION_PENDING:
                this.state = this.previousState;
                return emptyList();
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }

    /**
     * FIXME 确认修改订单,修改订单后，该订单的状态还是刚才的状态；修改只是一个临时状态；
     */
    public List<TicketDomainEvent> confirmReviseTicket(Map<String, Integer> revisedLineItemQuantities) {
        switch (state) {
            case REVISION_PENDING:
                this.state = this.previousState;
                return singletonList(new TicketRevised());
            default:
                throw new UnsupportedStateTransitionException(state);
        }
    }
}
