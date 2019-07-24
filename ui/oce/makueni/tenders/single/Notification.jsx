import { OverlayTrigger, Tooltip } from 'react-bootstrap';

class Notification extends React.Component {
  
  render() {
    const { data } = this.props;
  
    if (data === undefined) {
      return null;
    }
  
    const awardNotification = data[0];
  
    return (<div>
      <div className="row padding-top-10">
        <div className="col-md-4">
          <div className="item-label">Award Value</div>
          <div className="item-value">{awardNotification.awardValue}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">Date</div>
          <div className="item-value">{new Date(awardNotification.awardDate).toLocaleDateString()}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">Acknowledge Receipt of Award Timeline</div>
          <div className="item-value">{awardNotification.acknowledgementDays}</div>
        </div>
      </div>
  
      <div className="row padding-top-10">
        <div className="col-md-4">
          <div className="item-label">Supplier Name</div>
          <div className="item-value">{awardNotification.awardee.label}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">Winning Bid Supplier ID</div>
          <div className="item-value">{awardNotification.awardee.code}</div>
        </div>
        <div className="col-md-4">
          <div className="item-label">Supplier Postal Address</div>
          <div className="item-value">{awardNotification.awardee.address}</div>
        </div>
      </div>
  
      <div className="row padding-top-10">
        <div className="col-md-12">
          <div className="item-label">Letter of Notification of Award</div>
      
          {
            awardNotification.formDocs.map(doc => <div key={doc._id}>
              <OverlayTrigger
                placement="bottom"
                overlay={
                  <Tooltip id="download-tooltip">
                    Click to download the file
                  </Tooltip>
                }>
            
                <a className="item-value download" href={doc.url} target="_blank">
                  <i className="glyphicon glyphicon-download"/>
                  <span>{doc.name}</span>
                </a>
              </OverlayTrigger>
            </div>)
          }
        </div>
      </div>
    </div>);
  }
}

export default Notification;