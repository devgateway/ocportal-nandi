import PieChart from './pie-chart';

class BidsByItem extends PieChart{
  static getName(t){return t('charts:bidsByItem:title')}

  hoverTemplate() {
    return '%{customdata} invitations to bid for %{label}<br>%{percent} of all invitations to bid are for %{label}</br><extra></extra>';
  }

}

BidsByItem.endpoint = 'tendersByItemClassification';
BidsByItem.LABEL_FIELD = 'description';
BidsByItem.VALUE_FIELD = 'tenderCount';

export default BidsByItem;
