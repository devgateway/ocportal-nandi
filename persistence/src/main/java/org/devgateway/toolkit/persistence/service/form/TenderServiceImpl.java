package org.devgateway.toolkit.persistence.service.form;


import org.devgateway.toolkit.persistence.dao.form.Tender;
import org.devgateway.toolkit.persistence.repository.form.TenderRepository;
import org.devgateway.toolkit.persistence.repository.norepository.BaseJpaRepository;
import org.devgateway.toolkit.persistence.service.BaseJpaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gmutuhu
 *
 */
@Service
@Transactional(readOnly = true)
public class TenderServiceImpl extends BaseJpaServiceImpl<Tender> implements TenderService {

    @Autowired
    private TenderRepository tenderRepository;

    @Override
    protected BaseJpaRepository<Tender, Long> repository() {
        return tenderRepository;
    }

    @Override
    public Tender newInstance() {
        return new Tender();
    }

}