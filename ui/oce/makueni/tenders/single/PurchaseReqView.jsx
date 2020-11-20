import CRDPage from '../../../corruption-risk/page';
import {mtState} from '../state';
import cn from 'classnames';
import {API_ROOT} from '../../../state/oce-state';
import Tender from './Tender';
import PurchaseReq from './PurchaseReq';
import TenderQuotation from './TenderQuotation';
import ProfessionalOpinion from './ProfessionalOpinion';
import Notification from './Notification';
import Award from './Award';
import Contract from './Contract';
import React from 'react';
import FeedbackMessageList from '../../../feedback/feedbackList';
import AdministratorReport from './AdministratorReport';
import InspectionReport from './InspectionReport';
import PMCReport from './PMCReport';
import PaymentVoucher from './PaymentVoucher';
import MEReport from './MEReport';
import fmConnect from "../../../fm/fm";

class PurchaseReqView extends CRDPage {
  constructor(props) {
    super(props);

    this.state = {
      selected: 1
    };

    this.prID = mtState.input({
      name: 'prID',
    });

    this.prInfoUrl = mtState.mapping({
      name: 'prInfoUrl',
      deps: [this.prID],
      mapper: id => `${API_ROOT}/makueni/purchaseReq/id/${id}`,
    });

    this.prInfo = mtState.remote({
      name: 'prInfo',
      url: this.prInfoUrl,
    });

    this.tabs = [
      {
        name: 'Tender',
        tab: 1,
        fm: 'publicView.tender'
      }, {
        name: 'Purchase Requisitions',
        tab: 2,
        fm: 'publicView.tenderProcess'
      }, {
        name: 'Tender Evaluation',
        tab: 3,
        fm: 'publicView.tenderQuotationEvaluation'
      }, {
        name: 'Professional Opinion',
        tab: 4,
        fm: 'publicView.professionalOpinions'
      }, {
        name: 'Notification',
        tab: 5,
        fm: 'publicView.awardNotification'
      }, {
        name: 'Award',
        tab: 6,
        fm: 'publicView.awardAcceptance'
      }, {
        name: 'Contract',
        tab: 7,
        fm: 'publicView.contract'
      }, {
        name: 'Administrator Reports',
        tab: 8,
        fm: 'publicView.administratorReport'
      }, {
        name: 'Inspection Reports',
        tab: 9,
        fm: 'publicView.inspectionReport'
      }, {
        name: 'PMC Reports',
        tab: 10,
        fm: 'publicView.pmcReport'
      }, {
        name: 'M&E Reports',
        tab: 11,
        fm: 'publicView.meReport'
      }, {
        name: 'Payment Vouchers',
        tab: 12,
        fm: 'publicView.paymentVoucher'
      }
    ];

    this.isActive = this.isActive.bind(this);
    this.changeTab = this.changeTab.bind(this);
  }

  maybeTrimOcidPrefix(id) {
    if (id.includes("ocds-muq5cl-")) {
      return id.replace("ocds-muq5cl-", "");
    }
    return id;
  }

  componentDidMount() {
    super.componentDidMount();

    const {id} = this.props;

    this.prID.assign('PR', this.maybeTrimOcidPrefix(id));

    this.prInfo.addListener('PR', () => {
      this.prInfo.getState()
      .then(data => {
        this.setState({
          data: data.tenderProcesses,
          department: data.department,
          fiscalYear: data.fiscalYear
        });
      });
    });

    const {selected} = this.state;
    const visibleTabs = this.getVisibleTabs();
    if (visibleTabs.length > 0 && selected !== visibleTabs[0].tab) {
      this.changeTab(visibleTabs[0].tab);
    }
  }

  componentWillUnmount() {
    this.prInfo.removeListener('PR');
  }

  isActive(option) {
    const { selected } = this.state;
    if (selected === undefined || selected === '') {
      return false;
    }
    return selected === option;
  }

  changeTab(option) {
    this.setState({ selected: option });
  }

  displayTab() {
    const { selected, data, department, fiscalYear } = this.state;
    const tenderTitle = data.tender[0].tenderTitle;

    switch (selected) {
      case 1:
        return <Tender data={data.tender} prId={data._id} department={department} fiscalYear={fiscalYear}
                       styling={this.props.styling}/>;

      case 2:
        return <PurchaseReq data={data} department={department} fiscalYear={fiscalYear}
                            styling={this.props.styling}/>;

      case 3:
        return <TenderQuotation data={data.tenderQuotationEvaluation} tenderTitle={tenderTitle}
                                department={department} fiscalYear={fiscalYear}
                                styling={this.props.styling}/>;

      case 4:
        return <ProfessionalOpinion data={data.professionalOpinion} tenderTitle={tenderTitle}
                                    department={department} fiscalYear={fiscalYear}
                                    styling={this.props.styling}/>;

      case 5:
        return <Notification data={data.awardNotification} tenderTitle={tenderTitle}
                             department={department} fiscalYear={fiscalYear}
                             styling={this.props.styling}/>;

      case 6:
        return <Award data={data.awardAcceptance} department={department} tenderTitle={tenderTitle}
                      fiscalYear={fiscalYear} styling={this.props.styling}/>;

      case 7:
        return <Contract data={data.contract} department={department} tenderTitle={tenderTitle}
                         fiscalYear={fiscalYear} styling={this.props.styling}/>;

      case 8:
        return <AdministratorReport data={data.administratorReports} department={department}
                                     tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                                     styling={this.props.styling}/>;

      case 9:
        return <InspectionReport data={data.inspectionReports} department={department}
                                     tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                                     styling={this.props.styling}/>;

      case 10:
        return <PMCReport data={data.pmcReports} department={department}
                                 tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                                 styling={this.props.styling}/>;

      case 11:
        return <MEReport data={data.meReports} department={department}
                         tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                         styling={this.props.styling}/>;

      case 12:
        return <PaymentVoucher data={data.paymentVouchers} department={department}
                          tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                          styling={this.props.styling}/>;

      default:
        throw new Error("Tab not implemented");
    }
  }

  render() {
    const { navigate } = this.props;
    const { data } = this.state;

    const visibleTabs = this.getVisibleTabs();

    return (<div className="tender makueni-form">
      <div className="row">
        <div className="col-md-12 navigation-view" data-step="9" data-intro="Click to view and
        download the details and documents related to this tender process.">
          {
            visibleTabs.map(tab => {
              return (<a
                  key={tab.tab}
                  className={cn('', { active: this.isActive(tab.tab) })}
                  onClick={() => this.changeTab(tab.tab)}
                >
                  {tab.name}
                </a>
              );
            })
          }
        </div>
      </div>

      <div className="row">
        <a href="#!/tender" onClick={() => navigate()} className="back-link col-md-3">
        <span className="back-icon">
          <span className="previous">&#8249;</span>
        </span>
          <span>
          Go Back
        </span>
        </a>

          <div className="col-md-offset-5 col-md-4">
            <button className="btn btn-subscribe pull-right" type="submit"
                    data-step="11"
                    data-intro="Click this button to receive email alerts on any data updates
                    to this tender."
                    onClick={() => this.props.onSwitch('alerts', data._id, data.tender[0].tenderTitle)}>
              Receive Alerts For This Tender
            </button>
          </div>
      </div>


      {
        data !== undefined
          ? this.displayTab()
          : null
      }

      <div className="row">
        <p/>
        <FeedbackMessageList department={this.state.department}/>
      </div>
    </div>);
  }

  getVisibleTabs() {
    const { isFeatureVisible } = this.props;
    return this.tabs.filter(tab => isFeatureVisible(tab.fm));
  }
}

export default fmConnect(PurchaseReqView);
