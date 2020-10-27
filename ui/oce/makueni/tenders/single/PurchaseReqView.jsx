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

class PurchaseReqView extends CRDPage {
  constructor(props) {
    super(props);

    this.state = {
      selected: props.selected || ''
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
    this.tabs = [{
      name: this.t("purchaseReq:tabs:tender"),
      tab: 1
    }, {
      name: this.t("purchaseReq:tabs:purchaseReqs"),
      tab: 2
    }, {
      name: this.t("purchaseReq:tabs:tenderEvaluation"),
      tab: 3
    }, {
      name: this.t("purchaseReq:tabs:professionalOpinion"),
      tab: 4
    }, {
      name: this.t("purchaseReq:tabs:notification"),
      tab: 5
    }, {
      name: this.t("purchaseReq:tabs:award"),
      tab: 6
    }, {
      name: this.t("purchaseReq:tabs:contract"),
      tab: 7
    }, {
      name: this.t("purchaseReq:tabs:adminReports"),
      tab: 8
    },
      {
        name: this.t("purchaseReq:tabs:inspectionReports"),
        tab: 9
      },
      {
        name: this.t("purchaseReq:tabs:pmcReports"),
        tab: 10
      },
      {
        name: this.t("purchaseReq:tabs:meReports"),
        tab: 11
      },
      {
        name: this.t("purchaseReq:tabs:paymentVouchers"),
        tab: 12
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
                       styling={this.props.styling} translations={this.props.translations}/>;

      case 2:
        return <PurchaseReq data={data} department={department} fiscalYear={fiscalYear}
                            styling={this.props.styling} translations={this.props.translations}/>;

      case 3:
        return <TenderQuotation data={data.tenderQuotationEvaluation} tenderTitle={tenderTitle}
                                department={department} fiscalYear={fiscalYear}
                                styling={this.props.styling} translations={this.props.translations}/>;

      case 4:
        return <ProfessionalOpinion data={data.professionalOpinion} tenderTitle={tenderTitle}
                                    department={department} fiscalYear={fiscalYear}
                                    styling={this.props.styling} translations={this.props.translations}/>;

      case 5:
        return <Notification data={data.awardNotification} tenderTitle={tenderTitle}
                             department={department} fiscalYear={fiscalYear}
                             styling={this.props.styling} translations={this.props.translations}/>;

      case 6:
        return <Award data={data.awardAcceptance} department={department} tenderTitle={tenderTitle}
                      fiscalYear={fiscalYear} styling={this.props.styling} translations={this.props.translations}/>;

      case 7:
        return <Contract data={data.contract} department={department} tenderTitle={tenderTitle}
                         fiscalYear={fiscalYear} styling={this.props.styling} translations={this.props.translations}/>;

      case 8:
        return <AdministratorReport data={data.administratorReports} department={department}
                                     tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                                     styling={this.props.styling} translations={this.props.translations}/>;

      case 9:
        return <InspectionReport data={data.inspectionReports} department={department}
                                     tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                                     styling={this.props.styling} translations={this.props.translations}/>;

      case 10:
        return <PMCReport data={data.pmcReports} department={department}
                                 tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                                 styling={this.props.styling} translations={this.props.translations}/>;

      case 11:
        return <MEReport data={data.meReports} department={department}
                         tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                         styling={this.props.styling} translations={this.props.translations}/>;

      case 12:
        return <PaymentVoucher data={data.paymentVouchers} department={department}
                          tenderTitle={tenderTitle} fiscalYear={fiscalYear}
                          styling={this.props.styling} translations={this.props.translations}/>;

      default:
        return <Tender data={data.tender} department={department} tenderTitle={tenderTitle}
                       fiscalYear={fiscalYear} styling={this.props.styling} translations={this.props.translations}/>;
    }
  }

  render() {
    const { navigate } = this.props;
    const { data } = this.state;

    return (<div className="tender makueni-form">
      <div className="row">
        <div className="col-md-12 navigation-view" data-step="9" data-intro={this.t("purchaseReq:nav:dataIntro")}>
          {
            this.tabs.map(tab => {
              return (<a
                  key={tab.tab}
                  href="javascript:void(0);"
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
            {this.t("general:goBack")}
        </span>
        </a>

          <div className="col-md-offset-5 col-md-4">
            <button className="btn btn-subscribe pull-right" type="submit"
                    data-step="11"
                    data-intro={this.t("purchaseReq:receiveAlerts:dataIntro")}
                    onClick={() => this.props.onSwitch('alerts', data._id, data.tender[0].tenderTitle)}>
              {this.t("purchaseReq:receiveAlerts:caption")}
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
}

export default PurchaseReqView;
