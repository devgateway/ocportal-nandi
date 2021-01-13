import React from 'react';
import ReactDOM from 'react-dom';
import cn from 'classnames';
import { Map } from 'immutable';
import translatable from '../translatable';
import TotalFlags from './total-flags';
import { CORRUPTION_TYPES } from './constants';
import overviewBlue from '../resources/icons/blue/overview.svg';
import overviewWhite from '../resources/icons/white/overview.svg';
import suppliersBlue from '../resources/icons/blue/suppliers.svg';
import suppliersWhite from '../resources/icons/white/suppliers.svg';
import peBlue from '../resources/icons/blue/procuring-entities.svg';
import peWhite from '../resources/icons/white/procuring-entities.svg';
import buyersBlue from '../resources/icons/blue/buyers.svg';
import buyersWhite from '../resources/icons/white/buyers.svg';
import contractsBlue from '../resources/icons/blue/contracts.svg';
import contractsWhite from '../resources/icons/white/contracts.svg';
import fraudBlue from '../resources/icons/blue/FRAUD.svg';
import fraudWhite from '../resources/icons/white/FRAUD.svg';
import riggingBlue from '../resources/icons/blue/RIGGING.svg';
import riggingWhite from '../resources/icons/white/RIGGING.svg';
import collusionBlue from '../resources/icons/blue/COLLUSION.svg';
import collusionWhite from '../resources/icons/white/COLLUSION.svg';
import fmConnect from '../fm/fm';

const corruptionTypeIcons = {
  blue: { FRAUD: fraudBlue, RIGGING: riggingBlue, COLLUSION: collusionBlue },
  white: { FRAUD: fraudWhite, RIGGING: riggingWhite, COLLUSION: collusionWhite },
};

// eslint-disable-next-line no-undef
class Sidebar extends translatable(React.PureComponent) {
  componentDidMount() {
    const el = ReactDOM.findDOMNode(this);
    const scrollTarget = el.querySelector('div');
    const offsetTop = el.getBoundingClientRect().top;

    window.addEventListener('wheel', (e) => {
      let margin = parseInt(scrollTarget.style.marginTop, 10);
      if (margin == null || Number.isNaN(margin)) margin = 0;
      if (e.deltaY > 0) {
        margin -= 40;
      } else if (window.scrollY === 0) {
        margin += 40;
      }

      const newMargin = Math.min(
        0,
        Math.max(
          margin,
          window.innerHeight - scrollTarget.offsetHeight - offsetTop,
        ),
      );
      scrollTarget.style.marginTop = `${newMargin}px`;
    });
  }

  render() {
    const {
      page, indicatorTypesMapping, filters, years, monthly, months, navigate, translations,
      data, requestNewData, route, isFeatureVisible,
    } = this.props;

    return (
      <aside className="col-sm-3" id="crd-sidebar">
        <div>
          <section role="navigation" className="row">
            {isFeatureVisible('crd.sidebar.overview')
            && (
              <>
                <a
                  onClick={() => navigate()}
                  className={cn('crd-description-link', { active: !page })}
                >
                  <img className="blue" src={overviewBlue} alt="Overview icon" />
                  <img className="white" src={overviewWhite} alt="Overview icon" />
                  {this.t('tabs:overview:title')}
                </a>

                <p className="crd-description">
                  {this.t('crd:description')}
                </p>
              </>
            )}

            {isFeatureVisible('crd.sidebar.flags')
            && CORRUPTION_TYPES.filter((slug) => {
              const count = Object.keys(indicatorTypesMapping)
                .filter((key) => indicatorTypesMapping[key].types.indexOf(slug) > -1)
                .length;
              return count > 0;
            }).map((slug) => {
              let corruptionType;
              if (page === 'type' || page === 'indicator') {
                [, corruptionType] = route;
              }

              return (
                <a
                  onClick={() => navigate('type', slug)}
                  className={cn({ active: slug === corruptionType })}
                  key={slug}
                >
                  <img className="blue" src={corruptionTypeIcons.blue[slug]} alt="Tab icon" />
                  <img className="white" src={corruptionTypeIcons.white[slug]} alt="Tab icon" />
                  {this.t(`crd:corruptionType:${slug}:name`)}
                  {/* <span className="count">({count})</span> */}
                </a>
              );
            })}

            {isFeatureVisible('crd.sidebar.suppliers')
            && (
              <a
                onClick={() => navigate('suppliers')}
                className={cn('archive-link', { active: page === 'suppliers' || page === 'supplier' })}
                key="suppliers"
              >
                <img className="blue" src={suppliersBlue} alt="Suppliers icon" />
                <img className="white" src={suppliersWhite} alt="Suppliers icon" />
                {this.t('crd:contracts:baseInfo:suppliers')}
              </a>
            )}

            {isFeatureVisible('crd.sidebar.procuringEntities')
            && (
              <a
                onClick={() => navigate('procuring-entities')}
                className={cn('archive-link', { active: page === 'procuring-entities' || page === 'procuring-entity' })}
                key="procuring-entities"
              >
                <img className="blue" src={peBlue} alt="Procuring entities icon" />
                <img className="white" src={peWhite} alt="Procuring entities icon" />
                {this.t('crd:contracts:menu:procuringEntities')}
              </a>
            )}

            {isFeatureVisible('crd.sidebar.buyers')
            && (
              <a
                onClick={() => navigate('buyers')}
                className={cn('archive-link', { active: page === 'buyers' || page === 'buyer' })}
                key="buyers"
              >
                <img className="blue" src={buyersBlue} alt="Procuring entities icon" />
                <img className="white" src={buyersWhite} alt="Procuring entities icon" />
                {this.t('crd:contracts:menu:buyers')}
              </a>
            )}

            {isFeatureVisible('crd.sidebar.contracts')
            && (
              <a
                href="#!/crd/contracts"
                onClick={() => navigate('contracts')}
                className={cn('archive-link', 'contracts-link', { active: page === 'contracts' || page === 'contract' })}
                key="contracts"
              >
                <img className="blue" src={contractsBlue} alt="Contracts icon" />
                <img className="white" src={contractsWhite} alt="Contracts icon" />
                {this.t('crd:general:contracts')}
              </a>
            )}
          </section>
          <TotalFlags
            filters={filters}
            requestNewData={(path, newData) => requestNewData(['totalFlags'].concat(path), newData)}
            translations={translations}
            data={data.get('totalFlags', Map())}
            years={years}
            months={months}
            monthly={monthly}
            styling={this.props.styling}
          />
        </div>
      </aside>
    );
  }
}

export default fmConnect(Sidebar);