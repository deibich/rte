package org.deibic2s.ooka.rte.persistence;

import java.util.List;

interface IComponentDAO {
    List<ComponentDTO> getAllDataComponents();
    ComponentDTO getDataComponent(Integer componentID);
    boolean createDataComponent(ComponentDTO componentDTO);
    boolean removeDataComponent(ComponentDTO componentDTO);
    boolean updateDataComponent(ComponentDTO componentDTO);
    boolean removeAllDataComponents();
}
