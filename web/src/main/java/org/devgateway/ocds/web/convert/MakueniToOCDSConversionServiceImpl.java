package org.devgateway.ocds.web.convert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.WordUtils;
import org.devgateway.ocds.persistence.mongo.Address;
import org.devgateway.ocds.persistence.mongo.Amount;
import org.devgateway.ocds.persistence.mongo.Award;
import org.devgateway.ocds.persistence.mongo.Bids;
import org.devgateway.ocds.persistence.mongo.Budget;
import org.devgateway.ocds.persistence.mongo.Classification;
import org.devgateway.ocds.persistence.mongo.ContactPoint;
import org.devgateway.ocds.persistence.mongo.Contract;
import org.devgateway.ocds.persistence.mongo.Detail;
import org.devgateway.ocds.persistence.mongo.Document;
import org.devgateway.ocds.persistence.mongo.Identifier;
import org.devgateway.ocds.persistence.mongo.Item;
import org.devgateway.ocds.persistence.mongo.MakueniAward;
import org.devgateway.ocds.persistence.mongo.MakueniItem;
import org.devgateway.ocds.persistence.mongo.MakueniLocation;
import org.devgateway.ocds.persistence.mongo.MakueniLocationType;
import org.devgateway.ocds.persistence.mongo.MakueniOrganization;
import org.devgateway.ocds.persistence.mongo.MakueniPlanning;
import org.devgateway.ocds.persistence.mongo.MakueniTender;
import org.devgateway.ocds.persistence.mongo.Milestone;
import org.devgateway.ocds.persistence.mongo.Organization;
import org.devgateway.ocds.persistence.mongo.Period;
import org.devgateway.ocds.persistence.mongo.Planning;
import org.devgateway.ocds.persistence.mongo.Release;
import org.devgateway.ocds.persistence.mongo.Tag;
import org.devgateway.ocds.persistence.mongo.Tender;
import org.devgateway.ocds.persistence.mongo.Unit;
import org.devgateway.ocds.persistence.mongo.repository.main.MakueniLocationRepository;
import org.devgateway.ocds.persistence.mongo.repository.main.OrganizationRepository;
import org.devgateway.ocds.persistence.mongo.repository.main.ReleaseRepository;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.FileMetadata;
import org.devgateway.toolkit.persistence.dao.GenericPersistable;
import org.devgateway.toolkit.persistence.dao.categories.Category;
import org.devgateway.toolkit.persistence.dao.categories.Department;
import org.devgateway.toolkit.persistence.dao.categories.FiscalYear;
import org.devgateway.toolkit.persistence.dao.categories.LocationPointCategory;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethod;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity;
import org.devgateway.toolkit.persistence.dao.categories.Subcounty;
import org.devgateway.toolkit.persistence.dao.categories.Ward;
import org.devgateway.toolkit.persistence.dao.form.AbstractMakueniEntity;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptance;
import org.devgateway.toolkit.persistence.dao.form.AwardAcceptanceItem;
import org.devgateway.toolkit.persistence.dao.form.AwardNotification;
import org.devgateway.toolkit.persistence.dao.form.AwardNotificationItem;
import org.devgateway.toolkit.persistence.dao.form.Bid;
import org.devgateway.toolkit.persistence.dao.form.ContractDocument;
import org.devgateway.toolkit.persistence.dao.form.PlanItem;
import org.devgateway.toolkit.persistence.dao.form.ProcurementPlan;
import org.devgateway.toolkit.persistence.dao.form.Project;
import org.devgateway.toolkit.persistence.dao.form.PurchRequisition;
import org.devgateway.toolkit.persistence.dao.form.PurchaseItem;
import org.devgateway.toolkit.persistence.dao.form.Statusable;
import org.devgateway.toolkit.persistence.dao.form.TenderItem;
import org.devgateway.toolkit.persistence.dao.form.TenderProcess;
import org.devgateway.toolkit.persistence.dao.form.TenderQuotationEvaluation;
import org.devgateway.toolkit.persistence.service.form.TenderProcessService;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.devgateway.toolkit.persistence.dao.DBConstants.Status.APPROVED;

@Service
@Transactional(readOnly = true)
public class MakueniToOCDSConversionServiceImpl implements MakueniToOCDSConversionService {

    private static final Logger logger = LoggerFactory.getLogger(MakueniToOCDSConversionServiceImpl.class);

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private static final String OCID_PREFIX = "ocds-muq5cl-";

    private ImmutableMap<String, Tender.ProcurementMethod> procurementMethodMap;

    @Autowired
    private MongoFileStorageService mongoFileStorageService;

    @Autowired
    private TenderProcessService tenderProcessService;

    @Autowired
    private MakueniLocationRepository makueniLocationRepository;


    public MakueniLocation lpcToMakueniLocation(LocationPointCategory lpc) {
        MakueniLocation makueniLocation = makueniLocationRepository.findByDescription(lpc.getLabel());
        if (makueniLocation != null) {
            return makueniLocation;
        }
        MakueniLocation ml = new MakueniLocation();
        ml.setDescription(lpc.getLabel());
        if (lpc instanceof Ward) {
            ml.setType(MakueniLocationType.ward);
        } else if (lpc instanceof Subcounty) {
            ml.setType(MakueniLocationType.subcounty);
        } else {
            throw new RuntimeException("Unkown Makueni location type");
        }
        ml.setGeometry(new GeoJsonPoint(lpc.getLocationPoint().getX(), lpc.getLocationPoint().getY()));
        makueniLocation = makueniLocationRepository.save(ml);
        return makueniLocation;
    }

    public MakueniTender createTender(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        MakueniTender ocdsTender = new MakueniTender();
        safeSet(ocdsTender::setId, tender::getId, this::longIdToString);
        safeSet(ocdsTender::setTitle, tender::getTitle);
        safeSet(ocdsTender::setTenderPeriod, () -> tender, this::createTenderPeriod);
        safeSet(ocdsTender::setProcurementMethod, tender::getProcurementMethod, this::createProcurementMethod);
        safeSetEach(ocdsTender.getItems()::add, tender::getTenderItems, this::createTenderItem);
        safeSet(ocdsTender::setDescription, tender::getObjective);
        safeSet(ocdsTender::setProcuringEntity, tender::getIssuedBy, this::convertProcuringEntity);
        safeSet(ocdsTender::setValue, tender::getTenderValue, this::convertAmount);
        safeSet(ocdsTender::setTargetGroup, tender::getTargetGroup, this::categoryLabel);
        safeSet(ocdsTender::setStatus, () -> tender, this::createTenderStatus);
        safeSet(
                ocdsTender::setNumberOfTenderers, () -> tender.getTenderProcess().getSingleTenderQuotationEvaluation(),
                this::convertNumberOfTenderers
        );

        safeSetEach(ocdsTender.getLocations()::add, tender.getProject()::getWards, this::lpcToMakueniLocation);
        safeSetEach(ocdsTender.getLocations()::add, tender.getProject()::getSubcounties, this::lpcToMakueniLocation);

        //documents
        safeSet(ocdsTender.getDocuments()::add, tender::getFormDoc, this::storeAsDocumentTenderNotice);
        safeSet(ocdsTender.getDocuments()::add, tender::getTenderLink, this::createDocumentFromUrlTenderNotice);
        safeSet(ocdsTender.getDocuments()::add, tender.getTenderProcess()::getFormDoc,
                this::storeAsDocumentApprovedPurchaseRequisition
        );

        safeSetEach(
                ocdsTender.getDocuments()::add,
                () -> Optional.ofNullable(tender.getTenderProcess().getSingleTenderQuotationEvaluation())
                        .map(TenderQuotationEvaluation::getFormDocs).orElse(null),
                this::storeAsDocumentEvaluationReports
        );


        return ocdsTender;
    }


    public void addOrUpdateOrganizationSetByRole(HashMap<String, Organization> orgs, Organization o) {
        if (ObjectUtils.isEmpty(o)) {
            return;
        }
        if (!orgs.containsKey(o.getId())) {
            orgs.put(o.getId(), o);
        } else {
            orgs.get(o.getId()).getRoles().addAll(o.getRoles());
        }
    }

    public Collection<Organization> createParties(Release release) {
        HashMap<String, Organization> orgs = new HashMap<>();
        addOrUpdateOrganizationSetByRole(orgs, release.getBuyer());

        if (!ObjectUtils.isEmpty(release.getTender())) {
            addOrUpdateOrganizationSetByRole(orgs, release.getTender().getProcuringEntity());
        }

        if (!ObjectUtils.isEmpty(release.getAwards())) {
            release.getAwards().stream().filter(a -> !a.getSuppliers().isEmpty())
                    .flatMap(a -> a.getSuppliers().stream()).forEach(
                    s -> addOrUpdateOrganizationSetByRole(orgs, s));
        }

        return orgs.values();
    }


    public Integer convertNumberOfTenderers(TenderQuotationEvaluation tenderQuotationEvaluation) {
        return tenderQuotationEvaluation.getBids().size();
    }

    public Tender.Status createTenderStatus(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        if (tender.getTenderProcess().isTerminated()) {
            return Tender.Status.cancelled;
        }
        //TODO: finish this !!
        return Tender.Status.active;
    }

    public Organization convertBuyer(Department department) {
        Organization ocdsBuyer = new Organization();
        safeSet(ocdsBuyer::setName, () -> department, this::categoryLabel, WordUtils::capitalizeFully);
        safeSet(ocdsBuyer::setId, () -> department, this::entityIdToString);
        safeSet(ocdsBuyer::setIdentifier, () -> department, this::convertCategoryCodeToIdentifier);
        safeSet(ocdsBuyer.getAdditionalIdentifiers()::add, () -> department, this::convertCategoryToIdentifier);
//        safeSet(
//                ocdsBuyer.getAdditionalIdentifiers()::add, () -> department,
//                this::convertCategoryCodeToIdentifier
//        );
        safeSet(ocdsBuyer.getRoles()::add, () -> Organization.OrganizationType.buyer,
                Organization.OrganizationType::toValue
        );
        return ocdsBuyer;
    }

    public String entityIdToString(GenericPersistable p) {
        return p.getId().toString();
    }


    public Organization convertProcuringEntity(ProcuringEntity procuringEntity) {
        Organization ocdsProcuringEntity = new Organization();
        safeSet(ocdsProcuringEntity::setId, procuringEntity::getId, this::longIdToString);
        safeSet(ocdsProcuringEntity::setIdentifier, () -> procuringEntity, this::convertCategoryToIdentifier);
//        safeSet(
//                ocdsProcuringEntity.getAdditionalIdentifiers()::add, () -> procuringEntity,
//                this::convertCategoryCodeToIdentifier
//        );
        safeSet(ocdsProcuringEntity::setAddress, () -> procuringEntity, this::createProcuringEntityAddress);
        safeSet(ocdsProcuringEntity::setName, procuringEntity::getLabel, WordUtils::capitalizeFully);
        safeSet(ocdsProcuringEntity::setContactPoint, () -> procuringEntity, this::createProcuringEntityContactPoint);
        safeSet(ocdsProcuringEntity.getRoles()::add, () -> Organization.OrganizationType.procuringEntity,
                Organization.OrganizationType::toValue
        );

        return ocdsProcuringEntity;
    }

    public String categoryLabel(Category category) {
        return category.getLabel();
    }

    public ContactPoint createProcuringEntityContactPoint(ProcuringEntity procuringEntity) {
        ContactPoint ocdsContactPoint = new ContactPoint();
        safeSet(ocdsContactPoint::setEmail, procuringEntity::getEmailAddress);
        return ocdsContactPoint;
    }

    public Address createProcuringEntityAddress(ProcuringEntity procuringEntity) {
        Address ocdsAddress = new Address();
        safeSet(ocdsAddress::setCountryName, this::getCountry);
        safeSet(ocdsAddress::setStreetAddress, procuringEntity::getAddress);
        return ocdsAddress;
    }

    public String getCountry() {
        return "Kenya";
    }


    public Item createTenderItem(TenderItem tenderItem) {
        Item ocdsItem = new Item();
        safeSet(ocdsItem::setId, tenderItem::getId, this::longIdToString);
        safeSet(ocdsItem::setUnit, () -> tenderItem, this::createTenderItemUnit);
        safeSet(ocdsItem::setQuantity, tenderItem::getQuantity, BigDecimal::doubleValue);
        safeSet(ocdsItem::setClassification, tenderItem::getPurchaseItem, this::createPurchaseItemClassification);
        return ocdsItem;
    }


    public Unit createTenderItemUnit(TenderItem tenderItem) {
        Unit unit = new Unit();
        safeSet(unit::setScheme, () -> "scheme");
        safeSet(
                unit::setName, tenderItem::getPurchaseItem, PurchaseItem::getPlanItem, PlanItem::getUnitOfIssue,
                Category::getLabel
        );
        safeSet(unit::setId, tenderItem::getId, this::longIdToString);
        safeSet(unit::setValue, tenderItem::getUnitPrice, this::convertAmount);
        return unit;
    }

    public Classification createPurchaseItemClassification(PurchaseItem purchaseItem) {
        Classification classification = new Classification();
        safeSet(classification::setId, purchaseItem::getPlanItem, PlanItem::getItem, Category::getCode
        );
        safeSet(classification::setDescription, purchaseItem::getPlanItem, PlanItem::getItem, Category::getLabel,
                WordUtils::capitalizeFully
        );
        return classification;
    }

    public Tender.ProcurementMethod createProcurementMethod(ProcurementMethod procurementMethod) {
        return procurementMethodMap.get(procurementMethod.getLabel());
    }

    @PostConstruct
    public void init() {
        procurementMethodMap = ImmutableMap.<String, Tender.ProcurementMethod>builder()
                .put("Direct", Tender.ProcurementMethod.direct)
                .put("Open tender national", Tender.ProcurementMethod.open)
                .put("RF proposal", Tender.ProcurementMethod.limited)
                .put("RFQ", Tender.ProcurementMethod.selective)
                .put("Restricted tender", Tender.ProcurementMethod.limited)
                .put("Special permitted", Tender.ProcurementMethod.limited)
                .put("Low value procurement", Tender.ProcurementMethod.direct)
                .put("Framework Agreement", Tender.ProcurementMethod.direct)
                .put("Two-stage Tendering", Tender.ProcurementMethod.selective)
                .put("Design Competition", Tender.ProcurementMethod.selective)
                .put("Force Account", Tender.ProcurementMethod.direct)
                .put("Electronic Reverse Auction", Tender.ProcurementMethod.selective)
                .put("Open tender International", Tender.ProcurementMethod.open).build();
    }

    public Period createTenderPeriod(org.devgateway.toolkit.persistence.dao.form.Tender tender) {
        Period period = new Period();
        safeSet(period::setStartDate, tender::getInvitationDate);
        safeSet(period::setEndDate, tender::getClosingDate);
        return period;
    }


    public Budget createPlanningBudget(TenderProcess tenderProcess) {
        Budget budget = new Budget();

        safeSet(budget::setProjectID, tenderProcess.getProject()::getProjectTitle);
        safeSet(budget::setAmount, tenderProcess.getProject()::getAmountBudgeted, this::convertAmount);

        return budget;
    }


    private Document storeAsDocumentProcurementPlan(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm, Document.DocumentType.PROCUREMENT_PLAN);
    }

    private Document storeAsDocumentProjectPlan(AbstractMakueniEntity entity) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                entity.getFormDoc(),
                Document.DocumentType.PROJECT_PLAN
        );
    }

    private Document storeAsDocumentTenderNotice(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm, Document.DocumentType.TENDER_NOTICE);
    }


    private Document storeAsDocumentAwardNotice(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm, Document.DocumentType.AWARD_NOTICE);
    }

    private Document storeAsDocumentProfessionalOpinion(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm,
                Document.DocumentType.X_EVALUATION_PROFESSIONAL_OPINION
        );
    }

    private Document storeAsDocumentAwardAcceptance(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm,
                Document.DocumentType.X_AWARD_ACCEPTANCE
        );
    }


    private Document storeAsDocumentEvaluationReports(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                fm, Document.DocumentType.EVALUATION_REPORTS);
    }

    private Document storeAsDocumentApprovedPurchaseRequisition(FileMetadata fm) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(fm,
                Document.DocumentType.X_APPROVED_PURCHASE_REQUISITION);
    }

    private Document storeAsDocumentContractNotice(ContractDocument contractDocument) {
        return mongoFileStorageService.storeFileAndReferenceAsDocument(
                PersistenceUtil.getNext(contractDocument.getFormDocs()),
                contractDocument.getContractDocumentType().getLabel()
        );
    }


    private Document createDocumentFromUrlTenderNotice(String url) {
        return createDocumentFromUrl(url, Document.DocumentType.TENDER_NOTICE);
    }

    private Document createDocumentFromUrl(String url, Document.DocumentType documentType) {
        Document document = new Document();
        safeSet(document::setId, () -> url);
        safeSet(document::setUrl, () -> url, URI::create);
        safeSet(document::setDocumentType, documentType::toString);
        return document;
    }

    public MakueniPlanning createPlanning(TenderProcess tenderProcess) {
        MakueniPlanning planning = new MakueniPlanning();

        safeSet(planning::setBudget, () -> tenderProcess, this::createPlanningBudget);
        safeSet(planning::setFiscalYear, tenderProcess::getProcurementPlan, ProcurementPlan::getFiscalYear,
                FiscalYear::getLabel
        );

        safeSetEach(planning.getItems()::add, tenderProcess::getPurchaseItems, this::createPlanningItem);

        safeSet(planning.getDocuments()::add, tenderProcess.getProcurementPlan()::getFormDoc,
                this::storeAsDocumentProcurementPlan
        );

        safeSet(planning.getDocuments()::add, tenderProcess.getProject()::getCabinetPaper,
                this::storeAsDocumentProjectPlan
        );

        safeSetEach(planning.getMilestones()::add, tenderProcess::getPurchRequisitions, this::createPlanningMilestone);

        return planning;
    }


    public MakueniOrganization convertSupplier(org.devgateway.toolkit.persistence.dao.categories.Supplier supplier) {
        MakueniOrganization ocdsOrg = new MakueniOrganization();
        safeSet(ocdsOrg::setName, supplier::getLabel, WordUtils::capitalizeFully);
        safeSet(ocdsOrg::setId, () -> supplier, this::entityIdToString);
        safeSet(ocdsOrg::setIdentifier, () -> supplier, this::convertCategoryCodeToIdentifier);
        safeSet(ocdsOrg.getAdditionalIdentifiers()::add, () -> supplier, this::convertCategoryToIdentifier);
        safeSet(ocdsOrg::setAddress, () -> supplier, this::createSupplierAddress);
        safeSet(ocdsOrg.getRoles()::add, () -> Organization.OrganizationType.supplier,
                Organization.OrganizationType::toValue
        );
        safeSet(ocdsOrg::setTargetGroup, supplier::getTargetGroup, this::categoryLabel);
        return ocdsOrg;
    }

    public Address createSupplierAddress(org.devgateway.toolkit.persistence.dao.categories.Supplier supplier) {
        Address ocdsAddress = new Address();
        safeSet(ocdsAddress::setCountryName, this::getCountry);
        safeSet(ocdsAddress::setStreetAddress, supplier::getAddress);
        return ocdsAddress;
    }


    public Identifier convertCategoryToIdentifier(Category category) {
        Identifier identifier = new Identifier();
        safeSet(identifier::setId, category::getId, this::longIdToString);
        safeSet(identifier::setLegalName, category::getLabel);
        return identifier;
    }

    public Identifier convertCategoryCodeToIdentifier(Category category) {
        Identifier identifier = new Identifier();
        safeSet(identifier::setId, category::getCode);
        safeSet(identifier::setLegalName, category::getLabel);
        return identifier;
    }


    public Milestone createPlanningMilestone(PurchRequisition pr) {
        Milestone milestone = new Milestone();
        safeSet(milestone::setType, () -> Milestone.MilestoneType.PRE_PROCUREMENT, Milestone.MilestoneType::toValue);
        safeSet(milestone::setCode, () -> "approvedDate");
        safeSet(milestone::setId, pr::getId, this::longIdToString);
        safeSet(milestone::setDateMet, pr::getApprovedDate);
        safeSet(milestone::setStatus, () -> pr, this::createPlanningMilestoneStatus);
        return milestone;
    }

    @Override
    public Release createAndPersistRelease(TenderProcess tenderProcess) {
        try {
            Release release = createRelease(tenderProcess);
            Release byOcid = releaseRepository.findByOcid(release.getOcid());
            if (byOcid != null) {
                releaseRepository.delete(byOcid);
            }
            Release save = releaseRepository.save(release);
            logger.info("Saved " + save.getOcid());
            return save;
        } catch (Exception e) {
            logger.info("Exception processing tender process with id " + tenderProcess.getId());
            throw e;
        }
    }

    @Override
    public void convertToOcdsAndSaveAllApprovedPurchaseRequisitions() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        releaseRepository.deleteAll();
        organizationRepository.deleteAll();
        tenderProcessService.findAllStream().filter(Statusable::isExportable).forEach(this::createAndPersistRelease);
        postProcess();
        stopWatch.stop();
        logger.info("OCDS export finished in: " + stopWatch.getTime() + "ms");
    }

    public void postProcess() {
        applyFirstTimeWinners();
    }

    public void applyFirstTimeWinners() {
        Stream<Release> allOrderByEndDateStream =
                releaseRepository.findAllNonEmptyEndDatesAwardSuppliersOrderByEndDateDesc();
        Set<String> org = Sets.newConcurrentHashSet();

        allOrderByEndDateStream.forEach(r -> {
            MakueniAward award = (MakueniAward) r.getAwards().iterator().next();
            Organization supplier = award.getSuppliers().iterator().next();
            if (!org.contains(supplier.getId())) {
                org.add(supplier.getId());
                award.setFirstTimeWinner(true);
            } else {
                award.setFirstTimeWinner(false);
            }
            releaseRepository.save(r); //this is not very efficient, we should use update
        });
    }

    public Milestone.Status createPlanningMilestoneStatus(PurchRequisition pr) {
        //TODO: implement more statuses
        return Milestone.Status.SCHEDULED;
    }

    public <C, S, R extends Supplier<S>> Supplier<S> getSupplier(Supplier<C> parentSupplier,
                                                                 Function<C, R> childSupplier) {
        C c = parentSupplier.get();

        if (!ObjectUtils.isEmpty(c)) {
            return childSupplier.apply(c);
        }
        return null;
    }

    /**
     * Will set value only if not empty, but first it converts it using converter
     *
     * @param consumer
     * @param supplier
     * @param converter
     * @param <S>
     * @param <C>
     */
    public <S, C> void safeSet(Consumer<C> consumer, Supplier<S> supplier, Function<S, C> converter) {
        if (supplier == null || consumer == null || converter == null) {
            return;
        }

        Optional<C> c = safeConvert(supplier, converter);

        c.ifPresent(consumer);
    }

    public <S, C> Optional<C> safeConvert(Supplier<S> supplier, Function<S, C> converter) {
        if (supplier == null || converter == null) {
            return Optional.empty();
        }
        S o = supplier.get();
        if (o instanceof Statusable && !((Statusable) o).isExportable()) {
            return Optional.empty();
        }
        if (!ObjectUtils.isEmpty(o)) {
            C converted = converter.apply(o);
            if (!ObjectUtils.isEmpty(converted)) {
                return Optional.ofNullable(converted);
            }
        }
        return Optional.empty();
    }

    public <S, C, X> void safeSet(Consumer<C> consumer, Supplier<S> supplier,
                                  Function<S, X> converter1, Function<X, C> converter2) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null) {
            return;
        }
        Optional<X> x = safeConvert(supplier, converter1);
        safeConvert(() -> x.orElse(null), converter2).ifPresent(consumer);
    }

    public <S, C, X, Y> void safeSet(Consumer<C> consumer, Supplier<S> supplier,
                                     Function<S, X> converter1, Function<X, Y> converter2, Function<Y, C> converter3) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null || converter3 == null) {
            return;
        }
        Optional<X> x = safeConvert(supplier, converter1);
        Optional<Y> y = safeConvert(() -> x.orElse(null), converter2);
        safeConvert(() -> y.orElse(null), converter3).ifPresent(consumer);
    }

    public <S, C, X> void safeSetEach(Consumer<C> consumer, Supplier<Collection<S>> supplier,
                                      Function<S, X> converter1,
                                      Function<X, C> converter2) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null) {
            return;
        }

        Collection<S> o = supplier.get();
        if (!ObjectUtils.isEmpty(o)) {
            o.stream().filter(e -> !(e instanceof Statusable) || ((Statusable) e).isExportable()).
                    map(converter1).filter(Objects::nonNull).
                    map(converter2).filter(Objects::nonNull).
                    forEach(consumer);
        }
    }

    public <S, C, X, Y> void safeSetEach(Consumer<C> consumer, Supplier<Collection<S>> supplier,
                                         Function<S, X> converter1, Function<X, Y> converter2,
                                         Function<Y, C> converter3) {
        if (supplier == null || consumer == null || converter1 == null || converter2 == null || converter3 == null) {
            return;
        }

        Collection<S> o = supplier.get();
        if (!ObjectUtils.isEmpty(o)) {
            o.stream().filter(e -> !(e instanceof Statusable) || ((Statusable) e).isExportable()).
                    map(converter1).filter(Objects::nonNull).
                    map(converter2).filter(Objects::nonNull).
                    map(converter3).filter(Objects::nonNull).
                    forEach(consumer);
        }
    }


    public <S, C> void safeSetEach(Consumer<C> consumer, Supplier<Collection<S>> supplier, Function<S, C> converter) {
        if (supplier == null || consumer == null || converter == null) {
            return;
        }

        Collection<S> o = supplier.get();
        if (!ObjectUtils.isEmpty(o)) {
            o.stream().filter(e -> !(e instanceof Statusable) || ((Statusable) e).isExportable()).
                    map(converter).filter(Objects::nonNull).forEach(consumer);
        }
    }

    public <S> void safeSetEach(Consumer<S> consumer, Supplier<Collection<S>> supplier) {
        safeSetEach(consumer, supplier, Function.identity());
    }


    /**
     * same as #safeSet(Consumer, Supplier, Function), but with {@link Function#identity()} as converter
     *
     * @param consumer
     * @param supplier
     * @param <S>
     */
    public <S> void safeSet(Consumer<S> consumer, Supplier<S> supplier) {
        safeSet(consumer, supplier, Function.identity());
    }

    /**
     * Converter for ids into string ids. We do not use Long::toString because method signature is not unique at
     * compile
     * time
     *
     * @param id
     * @return
     */
    public String longIdToString(Long id) {
        return id.toString();
    }

    public Unit createPlanningItemUnit(PurchaseItem purchaseItem) {
        Unit unit = new Unit();
        safeSet(unit::setScheme, () -> "scheme");
        safeSet(unit::setName, purchaseItem::getPlanItem, PlanItem::getUnitOfIssue, this::categoryLabel);
        safeSet(unit::setId, purchaseItem::getId, this::longIdToString);
        safeSet(unit::setValue, purchaseItem::getAmount, this::convertAmount);
        return unit;
    }


    public Amount convertAmount(BigDecimal sourceAmount) {
        Amount amount = new Amount();
        safeSet(amount::setAmount, () -> sourceAmount);
        safeSet(amount::setCurrency, this::getCurrency);
        return amount;
    }

    public MakueniItem createPlanningItem(PurchaseItem purchaseItem) {
        MakueniItem ocdsItem = new MakueniItem();
        safeSet(ocdsItem::setId, purchaseItem::getId, this::longIdToString);
        safeSet(ocdsItem::setDescription, purchaseItem::getLabel);
        safeSet(ocdsItem::setUnit, () -> purchaseItem, this::createPlanningItemUnit);
        safeSet(ocdsItem::setQuantity, purchaseItem::getQuantity, BigDecimal::doubleValue);
        safeSet(ocdsItem::setClassification, () -> purchaseItem, this::createPurchaseItemClassification);
        safeSet(ocdsItem::setTargetGroup, purchaseItem::getPlanItem, PlanItem::getTargetGroup,
                this::categoryLabel
        );
        safeSet(
                ocdsItem::setTargetGroupValue, purchaseItem::getPlanItem, PlanItem::getTargetGroupValue,
                this::convertAmount
        );
        return ocdsItem;
    }


    public Amount.Currency getCurrency() {
        return Amount.Currency.KES;
    }

    public Set<Organization> createParties(Tender tender, Planning planning) {
        return null;
    }

    public Bids createBids(TenderQuotationEvaluation quotationEvaluation) {
        Bids bids = new Bids();
        safeSetEach(bids.getDetails()::add, quotationEvaluation::getBids, this::createBidsDetail);
        return bids;

    }

    public Set<Organization> createTenderersFromBids(Bids bids) {
        return bids.getDetails().stream().flatMap(b -> b.getTenderers().stream()).collect(Collectors.toSet());
    }


    public Detail createBidsDetail(Bid bid) {
        Detail detail = new Detail();
        safeSet(detail.getTenderers()::add, bid::getSupplier, this::convertSupplier);
        safeSet(detail::setValue, bid::getQuotedAmount, this::convertAmount);
        safeSet(detail::setStatus, () -> bid, this::createBidStatus);
        return detail;
    }

    public String createBidStatus(Bid bid) {
        if (DBConstants.SupplierResponsiveness.PASS.equals(bid.getSupplierResponsiveness())) {
            return "valid";
        }

        return "disqualified";
    }


    public MakueniAward createAward(AwardNotification awardNotification) {
        MakueniAward ocdsAward = new MakueniAward();
        safeSet(ocdsAward::setTitle, awardNotification::getTenderProcess, TenderProcess::getSingleTender,
                org.devgateway.toolkit.persistence.dao.form.Tender::getTenderTitle
        );
        safeSet(ocdsAward::setId, awardNotification::getTenderProcess, TenderProcess::getSingleTender,
                org.devgateway.toolkit.persistence.dao.form.Tender::getTenderNumber
        );

        safeSet(ocdsAward::setDate, awardNotification::getAcceptedNotification, AwardNotificationItem::getAwardDate);
        safeSet(ocdsAward::setValue, awardNotification::getAcceptedNotification, AwardNotificationItem::getAwardValue,
                this::convertAmount
        );

        safeSet(
                ocdsAward.getSuppliers()::add, awardNotification::getAcceptedNotification,
                AwardNotificationItem::getAwardee, this::convertSupplier
        );

        safeSet(ocdsAward::setContractPeriod, awardNotification::getAcceptedNotification,
                AwardNotificationItem::getAcknowledgementDays, this::convertDaysToPeriod
        );

        safeSet(ocdsAward.getDocuments()::add, awardNotification::getFormDoc, this::storeAsDocumentAwardNotice);
        safeSet(
                ocdsAward.getDocuments()::add,
                awardNotification.getTenderProcess().getSingleProfessionalOpinion()::getFormDoc,
                this::storeAsDocumentProfessionalOpinion
        );

        safeSet(
                ocdsAward.getDocuments()::add,
                () -> Optional.ofNullable(awardNotification.getTenderProcess().getSingleAwardAcceptance())
                        .filter(Statusable::isExportable).map(AwardAcceptance::getFormDoc).orElse(null),
                this::storeAsDocumentAwardAcceptance
        );


        //this will overwrite the award value taken from award notification with a possible different award value
        //from award acceptance (if any)
        safeSet(
                ocdsAward::setValue,
                () -> Optional.ofNullable(awardNotification.getTenderProcess().getSingleAwardAcceptance())
                        .filter(Statusable::isExportable)
                        .filter(AwardAcceptance::hasAccepted)
                        .map(AwardAcceptance::getAcceptedAcceptance)
                        .map(AwardAcceptanceItem::getAcceptedAwardValue).orElse(null),
                this::convertAmount
        );

        //same as above, but awardee

        safeSet(ocdsAward.getSuppliers()::add, () -> Optional.ofNullable(awardNotification.getTenderProcess().
                getSingleAwardAcceptance())
                .filter(Statusable::isExportable)
                .map(AwardAcceptance::getAcceptedAcceptance)
                .map(AwardAcceptanceItem::getAwardee).orElse(null), this::convertSupplier);

        safeSet(ocdsAward::setStatus, () -> awardNotification, this::createAwardStatus);


        return ocdsAward;
    }

    public Contract createContract(org.devgateway.toolkit.persistence.dao.form.Contract contract) {
        Contract ocdsContract = new Contract();
        safeSet(ocdsContract::setId, contract::getReferenceNumber);
        safeSet(ocdsContract::setTitle, contract::getTenderProcess, TenderProcess::getSingleTender,
                org.devgateway.toolkit.persistence.dao.form.Tender::getTenderTitle
        );
        safeSet(ocdsContract::setDateSigned, contract::getContractDate);
        safeSet(ocdsContract::setPeriod, contract::getExpiryDate, this::convertContractEndDateToPeriod);
        safeSet(ocdsContract::setValue, contract::getContractValue, this::convertAmount);
        safeSet(ocdsContract::setDateSigned, contract::getApprovedDate);
        safeSetEach(ocdsContract.getDocuments()::add, contract::getContractDocs, this::storeAsDocumentContractNotice);
        safeSet(ocdsContract::setAwardID, contract::getTenderProcess, TenderProcess::getSingleTender,
                org.devgateway.toolkit.persistence.dao.form.Tender::getTenderNumber
        );
        safeSet(ocdsContract::setStatus, contract::getStatus, this::createContractStatus);

        return ocdsContract;
    }

    public Contract.Status createContractStatus(String contractStatus) {
        if (APPROVED.equals(contractStatus)) {
            return Contract.Status.active;
        }
        if (DBConstants.Status.TERMINATED.equals(contractStatus)) {
            return Contract.Status.cancelled;
        }

        return Contract.Status.pending;
    }

    /**
     * OCMAKU-174
     *
     * @param awardNotification
     * @return
     */
    public Award.Status createAwardStatus(AwardNotification awardNotification) {

        //Cancelled: if Terminated at Notification of award or later stage
        if (awardNotification.isTerminated()
                || Optional.ofNullable(awardNotification.getTenderProcess().getSingleAwardAcceptance())
                .map(AwardAcceptance::isTerminated).orElse(false)
                || Optional.ofNullable(awardNotification.getTenderProcess().getSingleContract())
                .map(org.devgateway.toolkit.persistence.dao.form.Contract::isTerminated).orElse(false)
        ) {
            return Award.Status.cancelled;
        }

        if (ObjectUtils.isEmpty(awardNotification.getAcceptedNotification())) {
            return Award.Status.unsuccessful;
        }

        //Active: When Acceptance of award has been approved
        if (APPROVED.equals(
                Optional.ofNullable(awardNotification.getTenderProcess().getSingleAwardAcceptance())
                        .map(Statusable::getStatus).orElse(null))) {
            return Award.Status.active;
        }

        return Award.Status.pending;
    }

    public Period convertDaysToPeriod(Integer days) {
        Period period = new Period();
        period.setDurationInDays(days);
        return period;
    }


    public Period convertContractEndDateToPeriod(Date endDate) {
        Period period = new Period();
        period.setEndDate(endDate);
        return period;
    }


    public String getOcid(TenderProcess tenderProcess) {
        return OCID_PREFIX + tenderProcess.getId();
    }


    public List<Tag> createReleaseTag(Release release) {
        List<Tag> tags = new ArrayList<>();

        if (!ObjectUtils.isEmpty(release.getPlanning())) {
            tags.add(Tag.planning);
        }

        if (!ObjectUtils.isEmpty(release.getTender())) {
            tags.add(Tag.tender);
        }

        if (!ObjectUtils.isEmpty(release.getContracts())) {
            tags.add(Tag.contract);
        }

        if (!ObjectUtils.isEmpty(release.getAwards())) {
            tags.add(Tag.award);
        }
        return tags;
    }

    public int getTenderersFromTender(Tender tender) {
        if (tender.getTenderers() != null) {
            return tender.getTenderers().size();
        }
        return 0;
    }

    @Override
    public Release createRelease(TenderProcess tenderProcess) {
        Release release = new Release();
        safeSet(release::setId, tenderProcess::getId, this::longIdToString);
        safeSet(release::setDepartmentId, tenderProcess::getDepartment, Category::getId
        );
        safeSet(release::setOcid, () -> tenderProcess, this::getOcid);
        safeSet(release::setPlanning, () -> tenderProcess, this::createPlanning);
        safeSet(release::setBids, tenderProcess::getSingleTenderQuotationEvaluation, this::createBids);
        safeSet(release::setTender, tenderProcess::getSingleTender, this::createTender);
        safeSet(release::setBuyer, tenderProcess::getProject, Project::getProcurementPlan,
                ProcurementPlan::getDepartment, this::convertBuyer
        );


        safeSet(release.getTender()::setTenderers, release::getBids, this::createTenderersFromBids);
        safeSet(release.getTender()::setNumberOfTenderers, release::getTender, this::getTenderersFromTender);

        safeSet(release.getAwards()::add, tenderProcess::getSingleAwardNotification, this::createAward);
        safeSet(release.getContracts()::add, tenderProcess::getSingleContract, this::createContract);
        safeSet(release::setDate, Instant::now, Date::from);
        safeSet(release.getParties()::addAll, () -> release, this::createParties);
        safeSet(release.getTag()::addAll, () -> release, this::createReleaseTag);
        safeSet(release::setInitiationType, () -> Release.InitiationType.tender);

        addPartiesToOrganizationCollection(release.getParties());
        addTenderersToOrganizationCollection(release.getTender().getTenderers());

        return release;
    }


    private void addPartiesToOrganizationCollection(Set<Organization> parties) {
        parties.forEach(p -> {
            Optional<Organization> organization = organizationRepository.findById(p.getId());
            if (organization.isPresent()) {
                if (!organization.get().getRoles().containsAll(p.getRoles())) {
                    organization.get().getRoles().addAll(p.getRoles());
                    organizationRepository.save(organization.get());
                }
            } else {
                organizationRepository.save(p);
            }

        });
    }

    private void addTenderersToOrganizationCollection(Set<Organization> tenderers) {
        tenderers.forEach(p -> {
            Optional<Organization> organization = organizationRepository.findById(p.getId());
            if (!organization.isPresent()) {
                organizationRepository.save(p);
            }
        });
    }

}
