import React from 'react';
import cn from 'classnames';
import { fromJS, Map, Set } from 'immutable';
import URI from 'urijs';
import PropTypes from 'prop-types';
import {
  download, fetchJson, pluck, range,
} from './tools';
import Filters from './filters';
import './style.scss';
import enFlag from './resources/flags/en_US.png';
import esFlag from './resources/flags/es_ES.png';
import frFlag from './resources/flags/fr_FR.png';
import exportIcon from './resources/icons/export.svg';

const ROLE_ADMIN = 'ROLE_ADMIN';

const EMPTY_ARRAY = [];

const localeFlags = {
  en_US: enFlag,
  es_ES: esFlag,
  fr_FR: frFlag,
};

// eslint-disable-next-line no-undef
class OCApp extends React.Component {
  constructor(props) {
    super(props);
    this.tabs = [];
    this.state = {
      dashboardSwitcherOpen: false,
      exporting: false,
      width: 0,
      currentTab: 0,
      compareBy: '',
      compareOpen: false,
      comparisonCriteriaValues: [],
      selectedYears: Set(),
      selectedMonths: Set(range(1, 12)),
      filters: {},
      data: fromJS({}),
      comparisonData: fromJS({}),
      bidTypes: fromJS({}),
      years: fromJS([]),
      user: {
        loggedIn: false,
        isAdmin: false,
      },
    };
    const { oceLocale } = localStorage;
    if (oceLocale && this.constructor.TRANSLATIONS[oceLocale]) {
      this.state.locale = oceLocale;
    } else {
      this.state.locale = 'en_US';
    }
  }

  componentDidMount() {
    this.fetchBidTypes();
    // this.fetchYears();
    // this.fetchUserInfo();

    // const calcYearsBarWidth = () => this.setState({
    //   width: document.querySelector('.years-bar').offsetWidth - 30,
    // });

    // calcYearsBarWidth();

    // window.addEventListener('resize', debounce(calcYearsBarWidth));
  }

  setLocale(locale) {
    this.setState({ locale });
    localStorage.oceLocale = locale;
  }

  monthsBar() {
    const { selectedMonths } = this.state;
    return range(1, 12)
      .map((month) => (
        <a
          key={month}
          className={cn({ active: selectedMonths.has(+month) })}
          onClick={() => this.setState({
            selectedMonths: selectedMonths.has(+month)
              ? selectedMonths.delete(+month)
              : selectedMonths.add(+month),
          })}
        >
          <i className="glyphicon glyphicon-ok-circle" />
          {' '}
          {this.t(`general:months:${month}`)}
        </a>
      ));
  }

  showMonths() {
    const { years, selectedYears } = this.state;
    return selectedYears.intersect(years)
      .count() === 1;
  }

  yearsBar() {
    const { years, selectedYears } = this.state;
    const toggleYear = (year) => this.setState({
      selectedYears: selectedYears.has(+year)
        ? selectedYears.delete(+year)
        : selectedYears.add(+year),
    });
    const toggleOthersYears = (year) => this.setState({
      selectedYears: selectedYears.count() === 1 && selectedYears.has(year)
        ? Set(years)
        : Set([year]),
    });
    return years.sort()
      .map((year) => (
        <a
          key={year}
          className={cn({ active: selectedYears.has(+year) })}
          onDoubleClick={() => toggleOthersYears(year)}
          onClick={(e) => (e.shiftKey ? toggleOthersYears(year) : toggleYear(year))}
        >
          <i className="glyphicon glyphicon-ok-circle" />
          {' '}
          {year}
          <span className="ctrl-click-hint">
            {this.t('yearsBar:ctrlClickHint')}
          </span>
        </a>
      ))
      .toArray();
  }

  content() {
    const { navigate } = this.props;
    const {
      filters, compareBy, comparisonCriteriaValues, currentTab, bidTypes, width, locale,
    } = this.state;
    const Tab = this.tabs[currentTab];
    return (
      <Tab
        filters={filters}
        onUpdate={(filters) => this.setState({ filters })}
        compareBy={compareBy}
        comparisonCriteriaValues={comparisonCriteriaValues}
        requestNewData={(path, data) => this.updateData([currentTab, ...path], data)}
        requestNewComparisonData={(path, data) => this.updateComparisonData([currentTab, ...path], data)}
        data={this.state.data.get(currentTab) || fromJS({})}
        comparisonData={this.state.comparisonData.get(currentTab) || fromJS({})}
        monthly={filters.monthly}
        years={filters.year || EMPTY_ARRAY}
        months={filters.month || EMPTY_ARRAY}
        bidTypes={bidTypes}
        width={width}
        navigate={navigate}
        translations={this.constructor.TRANSLATIONS[locale]}
        styling={this.constructor.STYLING}
      />
    );
  }

  updateComparisonData(path, data) {
    this.setState((state) => ({ comparisonData: state.comparisonData.setIn(path, data) }));
  }

  updateData(path, data) {
    this.setState((state) => ({ data: state.data.setIn(path, data) }));
  }

  navigation() {
    return this.tabs.map((tab, index) => this.navigationLink(tab, index));
  }

  navigationLink({ getName }, index) {
    return (
      <a
        key={index}
        className={cn('', { active: index === this.state.currentTab })}
        onClick={() => this.setState({ currentTab: index })}
      >
        {getName(this.t.bind(this))}
      </a>
    );
  }

  comparison() {
    const { compareBy, compareOpen } = this.state;

    return (
      <div className="filters col-md-12">
        <div className="row filter compare">
          <div
            className={cn('col-md-12 filter-header', { selected: compareOpen })}
            onClick={() => this.setState({ compareOpen: !compareOpen })}
          >
            <i className="glyphicon glyphicon-tasks pull-left" />
            <div className="pull-left title">{this.t('header:comparison:title')}</div>
            <div className={`pull-right toggler ${compareOpen ? 'up' : 'down'}`} />
          </div>

          {compareOpen
            ? (
              <div className="col-md-12 compare-content">
                <div className="row">
                  <div className="col-md-6">
                    <label
                      htmlFor="comparison-criteria-select"
                    >
                      {this.t('header:comparison:criteria')}
                    </label>
                  </div>
                  <div className="col-md-6">
                    <select
                      id="comparison-criteria-select"
                      className="form-control"
                      value={compareBy}
                      onChange={(e) => this.updateComparisonCriteria(e.target.value)}
                    >
                      {this.constructor.COMPARISON_TYPES.map(({ value, label }) => <option key={value} value={value}>{this.t(label)}</option>)}
                    </select>
                  </div>
                </div>
              </div>
            )
            : null}
        </div>
      </div>
    );
  }

  filters() {
    const { bidTypes, locale, filters } = this.state;
    return (
      <this.constructor.Filters
        onUpdate={(filters) => this.setState({ filters })}
        bidTypes={bidTypes}
        filters={filters}
        translations={this.constructor.TRANSLATIONS[locale]}
      />
    );
  }

  fetchUserInfo() {
    const noCacheUrl = new URI('/rest/userDashboards/getCurrentAuthenticatedUserDetails').addSearch('time', new Date());
    fetchJson(noCacheUrl)
      .then(
        ({ id, roles }) => this.setState({
          user: {
            loggedIn: true,
            isAdmin: roles.some(({ authority }) => authority === ROLE_ADMIN),
            id,
          },
        }),
      )
      .catch(
        () => this.setState({
          user: {
            loggedIn: false,
          },
        }),
      );
  }

  fetchYears() {
    fetchJson('/api/tendersAwardsYears')
      .then((data) => {
        const years = fromJS(data.map(pluck('_id')));
        this.setState({
          years,
          selectedYears: Set(years),
        });
      });
  }

  fetchBidTypes() {
    fetchJson('/api/ocds/bidType/all')
      .then((data) => this.setState({
        bidTypes: data.reduce((map, datum) => map.set(datum.id, datum.description), Map()),
      }));
  }

  updateComparisonCriteria(criteria) {
    this.setState({
      compareBy: criteria,
      comparisonCriteriaValues: [],
      comparisonData: fromJS({}),
    });
    if (!criteria) return;
    fetchJson(new URI('/api/costEffectivenessTenderAmount').addSearch({
      groupByCategory: criteria,
      pageSize: 3,
    }))
      .then((data) => this.setState({
        comparisonCriteriaValues: data.map((datum) => datum[0] || datum._id),
      }));
  }

  t(text) {
    const { locale } = this.state;
    return this.constructor.TRANSLATIONS[locale][text];
  }

  registerTab(tab) {
    this.tabs.push(tab);
  }

  loginBox() {
    if (this.state.user.loggedIn) {
      return (
        <a href="/preLogout?referrer=/ui/index.html">
          <i className="glyphicon glyphicon-user" />
          {' '}
          {this.t('general:logout')}
        </a>
      );
    }
    return (
      <a href="/login?referrer=/ui/index.html">
        <i className="glyphicon glyphicon-user" />
        {' '}
        {this.t('general:login')}
      </a>
    );
  }

  languageSwitcher() {
    const { TRANSLATIONS } = this.constructor;
    if (Object.keys(TRANSLATIONS).length <= 1) return null;
    return Object.keys(TRANSLATIONS)
      .map((locale) => (
        <img
          className="icon"
          src={localeFlags[locale]}
          alt={`${locale} flag`}
          onClick={() => this.setLocale(locale)}
          key={locale}
        />
      ));
  }

  downloadExcel() {
    const { filters, selectedYears: years, selectedMonths: months } = this.state;
    const onDone = () => this.setState({ exporting: false });
    this.setState({ exporting: true });
    download({
      ep: 'excelExport',
      filters,
      years,
      months,
      t: this.t.bind(this),
    })
      .then(onDone)
      .catch(onDone);
  }

  exportBtn() {
    const {
      filters, selectedYears, locale, selectedMonths,
    } = this.state;
    let url = new URI('/api/ocds/excelExport')
      .addSearch(filters)
      .addSearch('year', selectedYears.toArray())
      .addSearch('language', locale);

    if (selectedYears.count() === 1) {
      url = url.addSearch('month', selectedMonths && selectedMonths.toJS())
        .addSearch('monthly', true);
    }

    return (
      <div className="filters">
        <a className="export-link" href={url} download="export.zip">
          <img
            className="top-nav-icon"
            src={exportIcon}
            width="100%"
            height="100%"
            alt="export"
          />
          {this.t('export:export')}
          <i className="glyphicon glyphicon-menu-down" />
        </a>
      </div>
    );
  }

  toggleDashboardSwitcher(e) {
    e.stopPropagation();
    const { dashboardSwitcherOpen } = this.state;
    this.setState({ dashboardSwitcherOpen: !dashboardSwitcherOpen });
  }

  dashboardSwitcher() {
    const { dashboardSwitcherOpen } = this.state;
    const { onSwitch } = this.props;
    return (
      <div className={cn('dash-switcher-wrapper', { open: dashboardSwitcherOpen })}>
        <h1 onClick={(e) => this.toggleDashboardSwitcher(e)}>
          {this.t('general:title')}
          <i className="glyphicon glyphicon-menu-down" />
          <small>{this.t('general:subtitle')}</small>
        </h1>
        {dashboardSwitcherOpen
        && (
        <div className="dashboard-switcher">
          <a onClick={() => onSwitch('crd')}>
            Corruption Risk Dashboard
          </a>
        </div>
        )}
      </div>
    );
  }
}

OCApp.propTypes = {
  onSwitch: PropTypes.func.isRequired,
};

OCApp.TRANSLATIONS = {
  us: {},
};

OCApp.Filters = Filters;

OCApp.STYLING = {
  charts: {
    axisLabelColor: undefined,
    traceColors: [],
  },
};

OCApp.COMPARISON_TYPES = [{
  value: '',
  label: 'header:comparison:criteria:none',
}, {
  value: 'bidTypeId',
  label: 'header:comparison:criteria:bidType',
}, {
  value: 'procuringEntityId',
  label: 'header:comparison:criteria:procuringEntity',
}];

export default OCApp;