import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import { useImmer } from 'use-immer';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { Item } from './Item';
import fmConnect from '../../../fm/fm';
import FileDownloadLinks from './FileDownloadLinks';
import { getProject } from '../../../api/Api';
import { setImmer } from '../../../tools';
import GoBack from '../../../goback';

const Project = (props) => {
  const [data, updateData] = useImmer(undefined);
  const { isFeatureVisible } = props;
  const { currencyFormatter, formatDate } = props.styling.tables;
  const { t } = useTranslation();

  const { id } = useParams();

  useEffect(() => window.scrollTo(0, 0), []);

  useEffect(() => {
    getProject({ id }).then(setImmer(updateData));
  }, [id]);

  // eslint-disable-next-line no-unused-vars
  const getFeedbackSubject = () => {
    let metadata;
    if (data !== undefined) {
      metadata = ` - ${data.projects.projectTitle
      } - ${data.department.label
      } - ${data.fiscalYear.label}`;
    }
    return escape(t('project:feedbackSubject') + metadata);
  };

  return (
    <div className="project makueni-form">
      <div className="row">
        <div className="col-md-3">
          <GoBack t={t} />
        </div>
      </div>

      <div className="row padding-top-10">
        <div className="col-md-12">
          <h1 className="page-title">{t('project:pageTitle')}</h1>
        </div>
      </div>

      {
      data !== undefined
        ? (
          <div>
            <div className="row display-flex">
              {isFeatureVisible('publicView.project.projectTitle')
              && <Item label={t('project:title')} value={data.projects.projectTitle} col={6} />}

              {isFeatureVisible('publicView.project.cabinetPapers')
              && (
              <Item label={t('project:title')} col={6}>
                <FileDownloadLinks files={data.projects.cabinetPapers.flatMap((cp) => cp.formDocs)} useDash />
              </Item>
              )}

              {isFeatureVisible('publicView.project.amountBudgeted')
              && <Item label={t('project:amountBudgeted')} value={currencyFormatter(data.projects.amountBudgeted)} col={6} />}

              {isFeatureVisible('publicView.project.amountRequested')
              && <Item label={t('project:amountRequested')} value={currencyFormatter(data.projects.amountRequested)} col={6} />}

              {isFeatureVisible('publicView.project.subcounties')
              && (
              <Item
                label={t('project:subcounties')}
                value={data.projects.subcounties.map((item) => item.label).join(', ')}
                col={6}
              />
              )}

              {isFeatureVisible('publicView.project.wards')
              && (
              <Item
                label={t('project:wards')}
                value={data.projects.wards && data.projects.wards.map((item) => item.label).join(', ')}
                col={6}
              />
              )}

              {isFeatureVisible('publicView.project.approvedDate')
              && <Item label={t('project:approvedDate')} value={formatDate(data.projects.approvedDate)} col={6} />}
            </div>
          </div>
        )
        : null
    }
    </div>
  );
};

Project.propTypes = {
  isFeatureVisible: PropTypes.func.isRequired,
  styling: PropTypes.object.isRequired,
};

export default fmConnect(Project);
