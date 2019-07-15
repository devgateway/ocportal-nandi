import translatable from '../../translatable';
import Component from '../../pure-render-component';
import cn from 'classnames';
import FilterItemDep from './FilterItemDep';
import { ppFilters } from '../procurementPlan/state';

class FiltersWrapper extends translatable(Component) {
  constructor(props) {
    super(props);
    
    this.state = {
      expanded: new Set(),
    };
  }
  
  toggleItem(index) {
    const expanded = new Set(this.state.expanded);
    if (expanded.has(index)) {
      expanded.delete(index);
    } else {
      expanded.add(index);
    }
    
    this.setState({ expanded: expanded });
  }
  
  reset() {
    // TODO reset!
    this.props.onUpdate(Map());
  }
  
  listItems() {
    const { expanded } = this.state;
    return this.constructor.ITEMS.map((Item, index) => <div
        key={index}
        className="row filter">
        <div className={cn('col-md-12 filter-header', { selected: expanded.has(index) })}
             onClick={_ => this.toggleItem(index)}>
          <div className="pull-left title">{Item.getName(this.t.bind(this))}</div>
          <div className={'pull-right toggler ' + (expanded.has(index) ? 'up' : 'down')}></div>
        </div>
        
        {expanded.has(index)
          ? <div className="col-md-12">
            <Item filters={this.props.filters}/>
            
            <section className="buttons">
              <button className="btn btn-apply pull-right" onClick={e => onUpdate(this.state.state)}>
                {this.t('filters:apply')}
              </button>
              <button className="btn btn-reset pull-right" onClick={e => this.reset()}>
                {this.t('filters:reset')}
              </button>
            </section>
          </div>
          : null
        }
      </div>
    );
  }
  
  render() {
    return <div className={cn('filters', 'col-md-12')}>
      {this.listItems()}
    </div>;
  }
}

FiltersWrapper.ITEMS = [FilterItemDep];

export default FiltersWrapper;
