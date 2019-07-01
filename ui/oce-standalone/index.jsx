import { Map } from 'immutable';
import cn from 'classnames';
import ReactDOM from 'react-dom';
import OCApp from '../oce';
import OverviewTab from '../oce/tabs/overview';
import LocationTab from '../oce/tabs/location';
import CompetitivenessTab from '../oce/tabs/competitiveness';
import EfficiencyTab from '../oce/tabs/efficiency';
import EProcurementTab from '../oce/tabs/e-procurement';
import { fetchJson } from '../oce/tools';
import ViewSwitcher from '../oce/switcher.jsx';
import CorruptionRickDashboard from '../oce/corruption-risk';

import './style.less';

class OCEDemoLocation extends LocationTab {
  getHeight() {
    const TOP_OFFSET = 128;
    const BOTTOM_OFFSET = 66;
    return window.innerHeight - TOP_OFFSET - BOTTOM_OFFSET;
  }
}

OCEDemoLocation.CENTER = [37, -100];

class OCEMakueni extends OCApp {
  constructor(props) {
    super(props);
    this.registerTab(OverviewTab);
    this.registerTab(OCEDemoLocation);
    this.registerTab(CompetitivenessTab);
    this.registerTab(EfficiencyTab);
    this.registerTab(EProcurementTab);
  }
  
  fetchBidTypes() {
    fetchJson('/api/ocds/bidType/all')
    .then(data =>
      this.setState({
        bidTypes: data.reduce((map, datum) =>
          map.set(datum.id, datum.description), Map())
      })
    );
  }
  
  loginBox() {
    let linkUrl;
    let text;
    if (this.state.user.loggedIn) {
      linkUrl = '/preLogout?referrer=/ui/index.html';
      text = this.t('general:logout');
    } else {
      linkUrl = '/login?referrer=/ui/index.html';
      text = this.t('general:login');
    }
    return (
      <a href={linkUrl} className="login-logout">
        <button className="btn btn-default">
          {text}
        </button>
      </a>
    );
  }
  
  dashboardSwitcher() {
    const { dashboardSwitcherOpen } = this.state;
    const { onSwitch } = this.props;
    return (
      <div className={cn('dash-switcher-wrapper', { open: dashboardSwitcherOpen })}>
        <h1 onClick={this.toggleDashboardSwitcher.bind(this)}>
          <strong>Monitoring & Evaluation</strong> Toolkit
          <i className="glyphicon glyphicon-menu-down"/>
        </h1>
        {dashboardSwitcherOpen &&
        <div className="dashboard-switcher">
          <a href="javascript:void(0);" onClick={() => onSwitch('crd')}>
            Corruption Risk Dashboard
          </a>
        </div>
        }
      </div>
    );
  }
  
  exportBtn() {
    if (this.state.exporting) {
      return (
        <div className="export-progress">
          <div className="progress">
            <div className="progress-bar progress-bar-danger" role="progressbar"
                 style={{ width: '100%' }}>
              {this.t('export:exporting')}
            </div>
          </div>
        </div>
      );
    }
    return (<div>
        <span className="export-title">
          Download the Data
        </span>
        <div className="export-btn">
          <button className="btn btn-default" disabled>
            <i className="glyphicon glyphicon-download-alt"/>
          </button>
        </div>
      </div>
    );
  }
  
  languageSwitcher() {
    const { TRANSLATIONS } = this.constructor;
    const { locale: selectedLocale } = this.state;
    if (Object.keys(TRANSLATIONS).length <= 1) return null;
    return Object.keys(TRANSLATIONS)
    .map(locale => (
      <a key={locale}
         href="javascript:void(0);"
         onClick={() => this.setLocale(locale)}
         className={cn({ active: locale === selectedLocale })}
      >
        {locale.split('_')[0]}
      </a>
    ));
  }
  
  render() {
    return (
      <div className="container-fluid dashboard-default"
           onClick={() => this.setState({ menuBox: '' })}>
        <header className="branding row">
          <div className="col-sm-3">
            <div className="logo-wrapper">
              <img src="assets/makueni-logo.png" alt="Makueni"/>
            </div>
            
            <div className="header-icons language-switcher">
              {this.languageSwitcher()}
            </div>
          </div>
          
          <div className="col-sm-7">
            <div className="row">
              <div className="navigation">
                {this.navigation()}
              </div>
            </div>
          </div>
          
          <div className="col-sm-2">
            {this.loginBox()}
          </div>
        </header>
        <div className="header-tools row">
          <div className="col-md-3">
            {this.dashboardSwitcher()}
          </div>
          
          <div className="col-md-6">
          
          </div>
          <div className="col-md-3 export">
            {this.exportBtn()}
          </div>
        </div>
        
        <div className="row content">
          <div className="col-xs-4 col-md-3 menu">
            <div className="row">
              <div className="filters-hint col-md-12">
                {this.t('filters:hint')}
              </div>
              {this.filters()}
              {this.comparison()}
            </div>
          </div>
          
          <div className="col-xs-8 col-md-9">
            <div className="row">
              {this.content()}
            </div>
          </div>
        </div>
        
        {this.showMonths() && <div
          className="col-xs-offset-4 col-md-offset-3 col-xs-8 col-md-9 months-bar"
          role="navigation"
        >
          {this.monthsBar()}
        </div>}
        <div
          className="col-xs-offset-4 col-md-offset-3 col-xs-8 col-md-9 years-bar"
          role="navigation"
        >
          {this.yearsBar()}
        </div>
        <footer className="col-sm-12 main-footer">&nbsp;</footer>
      </div>
    );
  }
}

const translations = {
  en_US: require('../../web/public/languages/en_US.json'),
  es_ES: require('../../web/public/languages/es_ES.json'),
  fr_FR: require('../../web/public/languages/fr_FR.json'),
};

const BILLION = 1000000000;
const MILLION = 1000000;
const THOUSAND = 1000;
const formatNumber = number => number.toLocaleString(undefined, { maximumFractionDigits: 2 });

const styling = {
  charts: {
    axisLabelColor: '#cc3c3b',
    traceColors: ['#324d6e', '#ecac00', '#4b6f33'],
    hoverFormat: ',.2f',
    hoverFormatter: (number) => {
      if (typeof number === 'undefined') return number;
      let abs = Math.abs(number);
      if (abs >= BILLION) return formatNumber(number / BILLION) + 'B';
      if (abs >= MILLION) return formatNumber(number / MILLION) + 'M';
      if (abs >= THOUSAND) return formatNumber(number / THOUSAND) + 'K';
      return formatNumber(number);
    },
  },
  tables: {
    currencyFormatter: formatNumber,
  },
};

OCEMakueni.STYLING = styling;
OCEMakueni.TRANSLATIONS = translations;

CorruptionRickDashboard.STYLING = JSON.parse(JSON.stringify(styling));

CorruptionRickDashboard.STYLING.charts.traceColors = ['#234e6d', '#3f7499', '#80b1d3', '#afd5ee', '#d9effd'];

class OceSwitcher extends ViewSwitcher {
}

OceSwitcher.views['m-and-e'] = OCEMakueni;
OceSwitcher.views.crd = CorruptionRickDashboard;

ReactDOM.render(<OceSwitcher
  translations={translations.en_US}
  styling={styling}
/>, document.getElementById('dg-container'));