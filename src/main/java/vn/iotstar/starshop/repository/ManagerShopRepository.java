package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.enums.ShopStatus;

public interface ManagerShopRepository extends JpaRepository<Shop, Long> {

    @EntityGraph(attributePaths = "owner")
    @Query("""
            select s from Shop s
            where (:status is null or s.status = :status)
              and (:keyword = '' or lower(s.name) like lower(concat('%', :keyword, '%'))
                   or lower(s.owner.email) like lower(concat('%', :keyword, '%')))
            """)
    Page<Shop> search(@Param("keyword") String keyword, @Param("status") ShopStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    @Query("select s from Shop s where s.id = :id")
    Optional<Shop> findWithOwner(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shop s join fetch s.owner where s.id = :id")
    Optional<Shop> findForUpdate(@Param("id") Long id);

    long countByStatus(ShopStatus status);
}
