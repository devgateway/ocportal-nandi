import ReactDOM from 'react-dom';
import { Popup } from 'react-leaflet';
import cn from 'classnames';
import React from 'react';
import Location from '../location/marker';
import Component from '../../../pure-render-component';
import translatable from '../../../translatable';
import OverviewChart from '../../charts/overview';
import CostEffectiveness from '../../charts/cost-effectiveness';
import { cacheFn, download } from '../../../tools';
import ProcurementMethodChart from '../../charts/procurement-method';
// eslint-disable-next-line no-unused-vars
import styles from './style.scss';
import ProjectCount from '../../charts/project-count';
import AmountBudgeted from '../../charts/amount-budgeted';
import exportMap from '../../../resources/icons/export-map.svg';
import cameraMap from '../../../resources/icons/camera-map.svg';

const addTenderDeliveryLocationId = cacheFn(
  (filters, id) => ({
    ...filters,
    tenderLoc: id,
  }),
);

class Tab extends translatable(Component) {}

export class ChartTab extends Tab {
  constructor(props) {
    super(props);
    this.state = {
      chartData: null,
    };
  }

  static getMargins() {
    return {
      t: 0,
      l: 50,
      r: 10,
      b: 50,
    };
  }

  static getChartClass() { return ''; }

  render() {
    const {
      filters, styling, years, translations, data, monthly, months,
    } = this.props;
    const decoratedFilters = addTenderDeliveryLocationId(filters, data._id);
    return (
      <div className={cn('map-chart', this.constructor.getChartClass())}>
        <this.constructor.Chart
          filters={decoratedFilters}
          styling={styling}
          years={years}
          monthly={monthly}
          months={months}
          translations={translations}
          data={this.state.chartData}
          requestNewData={(_, chartData) => this.setState({ chartData })}
          height={250}
          style={{ width: '100%' }}
          layout={{
            autosize: true,
          }}
          margin={this.constructor.getMargins()}
          legend="h"
        />
      </div>
    );
  }
}

class LocationWrapper extends translatable(Component) {
  constructor(props) {
    super(props);
    this.state = {
      currentTab: 0,
    };
  }

  doExcelExport() {
    const { currentTab } = this.state;
    const {
      data, filters, years, months,
    } = this.props;
    const CurrentTab = this.constructor.TABS[currentTab];
    download({
      ep: CurrentTab.Chart.excelEP,
      filters: addTenderDeliveryLocationId(filters, data._id),
      years,
      months,
      t: (translationKey) => this.t(translationKey),
    });
  }

  render() {
    const { currentTab } = this.state;
    const {
      data, translations, filters, years, styling, monthly, months,
    } = this.props;
    const CurrentTab = this.constructor.TABS[currentTab];
    const t = (translationKey) => this.t(translationKey);
    return (
      <Location {...this.props}>
        <Popup className="tender-locations-popup">
          <div>
            <header>
              {CurrentTab.prototype instanceof ChartTab
                && (
                <span className="chart-tools">
                  <a tabIndex={-1} role="button" onClick={() => this.doExcelExport()}>
                    <img
                      src={exportMap}
                      alt="Export"
                      width="16"
                      height="16"
                    />
                  </a>
                  <a
                    tabIndex={-1}
                    role="button"
                    onClick={() => ReactDOM.findDOMNode(this.currentChart).querySelector('.modebar-btn:first-child').click()}
                  >
                    <img
                      src={cameraMap}
                      alt="Screenshot"
                    />
                  </a>
                </span>
                )}
              {data.name}
            </header>
            <div className="row">
              <div className="tabs-bar col-xs-4">
                {this.constructor.TABS.map((tab, index) => (
                  <div
                    key={tab.getName(t)}
                    className={cn({ active: index === currentTab })}
                    onClick={() => this.setState({ currentTab: index })}
                    role="button"
                    tabIndex={0}
                  >
                    <span className="text-white">{tab.getName(t)}</span>
                  </div>
                ))}
              </div>
              <div className="col-xs-8">
                <CurrentTab
                  data={data}
                  translations={translations}
                  filters={filters}
                  years={years}
                  monthly={monthly}
                  months={months}
                  styling={styling}
                  ref={(c) => { this.currentChart = c; }}
                />
              </div>
            </div>
          </div>
        </Popup>
      </Location>
    );
  }
}

export class OverviewTab extends Tab {
  static getName(t) { return t('maps:tenderLocations:tabs:overview:title'); }

  render() {
    const { data } = this.props;
    const { count, totalProjectsAmount } = data;
    return (
      <div>
        {/* <p> */}
        {/*  <strong>{this.t('maps:tenderLocations:tabs:overview:nrOfTenders')}</strong> {count} */}
        {/* </p> */}
        <p>
          <strong>{this.t('maps:tenderLocations:tabs:overview:nrOfProjects')}</strong>
          {' '}
          {count}
        </p>
        <p>
          <strong>{this.t('maps:tenderLocations:tabs:overview:totalFundingByLocation')}</strong>
          {' '}
          {totalProjectsAmount.toLocaleString()}
        </p>
      </div>
    );
  }
}

export class OverviewChartTab extends ChartTab {
  static getName(t) { return t('charts:overview:title'); }

  static getChartClass() { return 'overview'; }
}

// const capitalizeAxisTitles = Class => class extends Class {
//   getLayout() {
//     const layout = super.getLayout();
//     //layout.xaxis.title = layout.xaxis.title;
//     //layout.yaxis.title = layout.yaxis.title.text;
//     return layout;
//   }
// };

OverviewChartTab.Chart = OverviewChart;

export class CostEffectivenessTab extends ChartTab {
  static getName(t) { return t('charts:costEffectiveness:title'); }
}

CostEffectivenessTab.Chart = CostEffectiveness;

export class ProjectCountChartTab extends ChartTab {
  static getName(t) { return t('charts:projectCount:title'); }

  static getChartClass() { return 'overview'; }
}

ProjectCountChartTab.Chart = ProjectCount;

export class AmountBudgetedChartTab extends ChartTab {
  static getName(t) { return t('charts:amountBudgeted:title'); }

  static getChartClass() { return 'overview'; }
}

AmountBudgetedChartTab.Chart = AmountBudgeted;

export class ProcurementMethodTab extends ChartTab {
  static getName(t) { return t('charts:procurementMethod:title'); }
}

ProcurementMethodTab.Chart = ProcurementMethodChart;

LocationWrapper.TABS = [OverviewTab, ProjectCountChartTab, AmountBudgetedChartTab];

export default LocationWrapper;