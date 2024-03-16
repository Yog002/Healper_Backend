package cn.edu.tongji.healper.repository;

import cn.edu.tongji.healper.entity.ScaleRecordEntity;
import cn.edu.tongji.healper.outdto.ScaleRecordInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScaleRecordRepository extends JpaRepository<ScaleRecordEntity, Integer> {

    @Query(value = "select new cn.edu.tongji.healper.outdto.ScaleRecordInfo(s.id ,s.endTime," +
            "s.isHidden, s.scaleId, s.record, p.name) " +
            "from ScaleRecordEntity s inner join PsychologyScaleEntity p " +
            "on s.scaleId=p.id where s.clientId=?1 order by s.endTime desc")
    List<ScaleRecordInfo> findScaleRecordInfoByClientId(Integer clientId, Pageable pageable);

    @Query(value = "select new cn.edu.tongji.healper.outdto.ScaleRecordInfo(s.id, s.endTime, " +
            "s.isHidden, s.scaleId, s.record, p.name) " +
            "from ScaleRecordEntity s, PsychologyScaleEntity p where s.scaleId=p.id and s.id=?1")
    ScaleRecordInfo findScaleRecordInfoById(Integer id);

    Integer countScaleRecordEntitiesByClientId(Integer clientId);

    @Query(value = "select sre.record from ScaleRecordEntity sre where sre.clientId = ?1")
    List<String> findClientLabelsPriority(Integer clientId);
}
