package vn.iotstar.starshop.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.data.domain.*;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.config.NotificationSocket;
@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notifications;
    private final NotificationSocket socket;
    @Transactional
    public void send(User user,String title,String content,String url) {
        Notification n=new Notification();n.setUser(user);n.setTitle(title);n.setContent(content);n.setTargetUrl(url);
        notifications.save(n);
        String email=user.getEmail();
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {socket.notifyUser(email);}
            });
        else socket.notifyUser(email);
    }
    @Transactional
    public void orderChanged(Order order) {
        String content="Đơn #"+order.getId()+" · "+order.getStatus();
        send(order.getUser(),"Cập nhật đơn hàng",content,"/orders");
        send(order.getShop().getOwner(),"Cập nhật đơn shop",content,"/vendor/shops/"+order.getShop().getId()+"/orders");
    }
    @Transactional(readOnly=true)
    public Page<Notification> list(String email,int page) {
        return notifications.findByUser_EmailIgnoreCaseOrderByCreatedAtDesc(email,PageRequest.of(Math.max(0,page),10));
    }
    @Transactional
    public void markRead(Long id,String email) {
        notifications.findByIdAndUser_EmailIgnoreCase(id,email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo")).setReadStatus(true);
    }
    @Transactional(readOnly=true)
    public long unread(String email) {return notifications.countByUser_EmailIgnoreCaseAndReadStatusFalse(email);}
}
