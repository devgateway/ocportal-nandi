import { ControlLabel, Form, FormControl, FormGroup, HelpBlock, InputGroup } from 'react-bootstrap';
import React from 'react';
import { getFeedbackUrlPart } from './feedbackList';

class FeedbackMessageForm extends React.PureComponent {

  constructor(props) {
    super(props);

    this.state = {
      replyOpen: this.props.replyOpen,
      emailPattern: /^([\w.%+-]+)@([\w-]+\.)+([\w]{2,})$/i,
      email: '',
      name: '',
      comment: '',
      replyFor: this.props.replyFor,
      emailValid: true,
      error: false
    };
  }

  validateTxt(stateField) {
    if (stateField === '') {
      return 'error';
    } else {
      return 'success';
    }
  }


  validateEmail() {
    if (this.state.emailValid === true) {
      return undefined;
    }

    const emailValid = this.state.email.match(this.state.emailPattern);

    if (emailValid) {
      return 'success';
    } else {
      return 'error';
    }
  }

  handleChange(e) {
    const name = e.target.name;
    const value = e.target.value;

    this.setState({
      [name]: value,
      error: false
    });

    if (name === 'email') {
      this.setState({
        emailValid: this.state.email.match(this.state.emailPattern)
      });
    }
  }

  submit() {
    const { email, emailPattern, name, comment, replyFor } = this.state;
    let error = false;
    //const [purchaseReqId, tenderTitle] = this.props.route;

    if (!email.match(emailPattern) || !name) {
      error = true;

      this.setState({
        emailValid: email.match(emailPattern)
      });
    }


    if (error) {
      this.setState({ error });
      return 0;
    } else {
      this.setState({
        replyOpen: false,
        email: '',
        name: '',
        comment: '',
      });
      this.props.feedbackPoster({
        email: email,
        name: name,
        url: getFeedbackUrlPart(),
        comment: comment,
        replyFor: replyFor
      });
    }
  }

  renderReplyButton() {
    return (<button className="btn" onClick={this.handleReplyClick.bind(this)}>Reply</button>);
  }

  handleReplyClick() {
    this.setState({
      replyOpen: true
    });
  }

  renderForm() {
    return (
      <div className="col-md-6">
        <FormGroup validationState={this.validateEmail()}>
          <ControlLabel>Email</ControlLabel>
          <InputGroup>
            <InputGroup.Addon>@</InputGroup.Addon>
            <FormControl
              type="email"
              name="email"
              value={this.state.email}
              placeholder="Email address"
              onChange={this.handleChange.bind(this)}
            />
          </InputGroup>
          <FormControl.Feedback/>
          {
            this.state.emailValid
              ? null
              : <HelpBlock>Email is invalid</HelpBlock>
          }
        </FormGroup>

        <FormGroup validationState={this.validateTxt(this.state.name)}>
          <ControlLabel>Name</ControlLabel>
          <FormControl
            name="name"
            value={this.state.name}
            placeholder="Name"
            onChange={this.handleChange.bind(this)}
          />
          <FormControl.Feedback/>
          {
            this.state.name ? null : <HelpBlock>Please add a name</HelpBlock>
          }
        </FormGroup>

        <FormGroup validationState={this.validateTxt(this.state.comment)}>
          <ControlLabel>Comment</ControlLabel>
          <FormControl
            required
            componentClass="textarea"
            name="comment"
            rows={5}
            value={this.state.comment}
            placeholder="Comment"
            onChange={this.handleChange.bind(this)}
          />
          <FormControl.Feedback/>
          {
            this.state.comment ? null : <HelpBlock>Please add a comment</HelpBlock>
          }
        </FormGroup>

        <div className="row apply-button">
          <div className="col-md-6">
            <button className="btn btn-default" type="submit"
                    onClick={this.submit.bind(this)}>Send Feedback
            </button>
          </div>
        </div>
      </div>);
  }

  render() {
    return (
      <div>
        {this.state.replyOpen ? this.renderForm() : this.renderReplyButton()}
      </div>);
  }
}

export default FeedbackMessageForm;

