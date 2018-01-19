import URI from 'urijs';
import { List, Map } from 'immutable';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import CRDPage from '../page';
import PaginatedTable from '../paginated-table';
import Archive from '../archive';
import { wireProps } from '../tools';
import { fetchEP, pluckImm, cacheFn } from '../../tools';

export const mkLink = navigate => (content, { id }) => (
  <a
    href={`#!/crd/supplier/${id}`}
    onClick={() => navigate('supplier', id)}
  >
    {content}
  </a>
);

class SList extends PaginatedTable {
  getCustomEP() {
    const { searchQuery } = this.props;
    const eps = super.getCustomEP();
    return searchQuery ?
      eps.map(ep => ep.addSearch('text', searchQuery)) :
      eps;
  }

  componentDidUpdate(prevProps, prevState) {
    const propsChanged = ['filters', 'searchQuery'].some(key => this.props[key] !== prevProps[key]);
    if (propsChanged) {
      this.fetch();
    } else {
      super.componentDidUpdate(prevProps, prevState);
    }
  }

  render() {
    const { data, navigate } = this.props;

    const count = data.get('count', 0);

    const { pageSize, page } = this.state;

    const jsData = data.get('data', List()).map((supplier) => {
      return {
        id: supplier.get('id'),
        name: supplier.get('name'),
        wins: supplier.get('wins', 'n/a'),
        winAmount: supplier.get('winAmount', 'n/a'),
        losses: supplier.get('losses', 'n/a'),
        flags: supplier.get('flags', 'n/a'),
      }
    }).toJS();

    return (
      <BootstrapTable
        data={jsData}
        striped
        bordered={false}
        pagination
        remote
        fetchInfo={{
          dataTotalSize: count,
        }}
        options={{
          page,
          onPageChange: newPage => this.setState({ page: newPage }),
          sizePerPage: pageSize,
          sizePerPageList: [20, 50, 100, 200].map(value => ({ text: value, value })),
          onSizePerPageList: newPageSize => this.setState({ pageSize: newPageSize }),
          paginationPosition: 'both',
        }}
      >
        <TableHeaderColumn dataField='name' dataFormat={mkLink(navigate)}>
          Name
        </TableHeaderColumn>
        <TableHeaderColumn dataField='id' isKey dataFormat={mkLink(navigate)}>
          ID
        </TableHeaderColumn>
        <TableHeaderColumn dataField='wins'>
          Wins
        </TableHeaderColumn>
        <TableHeaderColumn dataField='winAmount'>
          Total won
        </TableHeaderColumn>
        <TableHeaderColumn dataField='losses'>
          Losses
        </TableHeaderColumn>
        <TableHeaderColumn dataField='flags'>
          Total No. Flags
        </TableHeaderColumn>
      </BootstrapTable>
    );
  }
}

class Suppliers extends CRDPage {
  constructor(...args){
    super(...args);
    this.state = this.state || {};
    this.state.winLossFlagInfo = Map();
    this.injectWinLossData = cacheFn((data, winLossFlagInfo) => {
      return data.update('data', List(), list => list.map(supplier => {
        const id = supplier.get('id');
        if (!winLossFlagInfo.has(id)) return supplier;
        const info = winLossFlagInfo.get(id);
        return supplier
          .set('wins', info.won.count)
          .set('winAmount', info.won.totalAmount)
          .set('losses', info.lostCount)
          .set('flags', info.applied.countFlags)
      }))
    });
  }

  onNewDataRequested(path, newData) {
    const supplierIds = newData.get('data').map(pluckImm('id'));
    this.setState({ winLossFlagInfo: Map() });
    fetchEP(new URI('/api/procurementsWonLost').addSearch({
      bidderId: supplierIds.toJS(),
    })).then(result => {
      this.setState({
        winLossFlagInfo: Map(supplierIds.zip(result))
      });
    });
    this.props.requestNewData(path, newData);
  }

  render() {
    const { navigate, searchQuery, doSearch, data } = this.props;
    const { winLossFlagInfo } = this.state;
    return (
      <Archive
        {...wireProps(this)}
        data={this.injectWinLossData(data, winLossFlagInfo)}
        requestNewData={this.onNewDataRequested.bind(this)}
        searchQuery={searchQuery}
        doSearch={doSearch}
        navigate={navigate}
        className="suppliers-page"
        topSearchPlaceholder={this.t('crd:suppliers:top-search')}
        List={SList}
        dataEP="ocds/organization/supplier/all"
        countEP="ocds/organization/supplier/count"
      />
    );
  }
}

export default Suppliers;
