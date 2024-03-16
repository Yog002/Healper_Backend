package cn.edu.tongji.healper.repository;

import cn.edu.tongji.healper.entity.ConsultHistoryEntity;
import cn.edu.tongji.healper.outdto.Archive;
import cn.edu.tongji.healper.outdto.ConsultOrder;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConsultHistoryRepository extends
        JpaRepository<ConsultHistoryEntity, Integer>,
        JpaSpecificationExecutor<ConsultHistoryEntity> {

    @Query("select new " +
            "cn.edu.tongji.healper.outdto.ConsultOrder(" +
            "che.id, che.startTime, che.endTime, " +
            "che.consultantId, che.clientId, ce.realname, che.expense, che.status, ce.sex, ce.age" +
            ") from ConsultHistoryEntity che, ConsultantEntity ce " +
            "where che.consultantId = ce.id and che.clientId = ?1"
    )
    List<ConsultOrder> findConsultOrderByClientId(Integer clientId);

    @Query("select new " +
            "cn.edu.tongji.healper.outdto.ConsultOrder(" +
            "che.id, che.startTime, che.endTime, " +
            "che.consultantId, che.clientId, ce.nickname, che.expense, che.status, " +
            "ce.sex, ce.age" +
            ") from ConsultHistoryEntity che, ClientEntity ce " +
            "where che.consultantId = ?1 and che.clientId = ce.id" +
            ""
    )
//    @Query(nativeQuery = true, value = "select " +
//            "ch.id as id, ch.start_time as startTime, ch.end_time as endTime, ch.consultant_id as consultantId, ch.client_id as clientId, c.nickname as nickname, ch.expense as expense, ch.status as status " +
//            "from consult_history as ch, client as c where ch.consultant_id = ?1 and ch.client_id = c.id")
    List<ConsultOrder> findConsultOrderByConsultantId(Integer consultantId);

    @Query("select ce.qrCodeLink " +
            "from ConsultHistoryEntity che, ConsultantEntity ce " +
            "where che.consultantId = ce.id and che.id = ?1"
    )
    String findQrCodeByHistoryId(Integer historyId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
            "update consult_history as ch set ch.status = ?2 where ch.id = ?1")
    Integer updateHistoryStatusById(Integer historyId, String status);


    @Query(value = "select new cn.edu.tongji.healper.outdto.Archive" +
            "(a.id, a.consultantId, a.endTime, a.expense, a.startTime, a.advice, a.summary, s.realname) " +
            "from ConsultHistoryEntity a inner join ConsultantEntity s " +
            "on a.consultantId=s.id where a.clientId=?1 and a.status='f' order by a.endTime desc")
    List<Archive> getArchivesByClientId(@Param("id") Integer id, Pageable pageable);

    ConsultHistoryEntity getConsultHistoryEntityById(Integer historyId);

    List<ConsultHistoryEntity> findConsultHistoryEntitiesByClientId(Integer clientId);

    @Query(value = "select count(a.id) from ConsultHistoryEntity a where a.clientId=?1 and a.status = 'f'")
    Integer getArchiveNumByClientId(@Param("id") Integer id);

    @Query(value = "select count(_order.id) from ConsultHistoryEntity _order where _order.clientId = ?1")
    Integer findOrderNumByClientId(Integer clientId);

    @Query(value = "select count(_order.id) from ConsultHistoryEntity _order where _order.consultantId = ?1")
    Integer findOrderNumByConsultantId(Integer consultantId);

    @Query(value = "select count(record.id) from ConsultHistoryEntity record where record.clientId = ?1")
    Integer findConsultRecordNumByClientId(Integer clientId);

    @Query("select new " +
            "cn.edu.tongji.healper.outdto.ConsultOrder(" +
            "che.id, che.startTime, che.endTime, " +
            "che.consultantId, che.clientId, ce.realname, che.expense, che.status, ce.sex, ce.age" +
            ") from ConsultHistoryEntity che, ConsultantEntity ce " +
            "where che.consultantId = ce.id and che.clientId = ?1 and (che.status = 'w' or che.status = 's' or che.status = 'p') order by che.startTime desc"
    )
    List<ConsultOrder> findWaitingConsultOrders(Integer clientId);

    @Query(value = "select che from ConsultHistoryEntity che where che.clientId = ?1 and che.consultantId = ?2 and (che.status = 'w' or che.status = 'p' or che.status = 's') order by che.startTime desc")
    ConsultHistoryEntity findFirstByClientIdAndConsultantId(Integer clientId, Integer consultantId);

}
