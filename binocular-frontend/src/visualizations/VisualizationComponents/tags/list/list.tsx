import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import 'status-indicator/styles.css';
import moment from 'moment';

import Collapsible from '../collapsible';
import Pagination from '../pagination';
import TagsFilter from '../tagsFilter';

import { changeTagsPage, requestTagCommits, requestTagsData } from '../sagas';
import styles from './list.module.scss';
import { Props } from './index';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

enum CategoriesState {
  VIEW = 'VIEW',
  SELECT = 'SELECT',
  WAITING = 'WAITING',
}

function List(props: Props) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const query = useQuery();

  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [withoutTags, setWithoutTags] = useState<boolean>(false);

  const [categoriesState, setCategoriesState] = useState<CategoriesState>(CategoriesState.VIEW);
  const [selectedCommits, setSelectedCommits] = useState<string[]>([]);
  const [openStates, setOpenStates] = useState<boolean[]>(props.commits ? props.commits.map(() => false) : []);
  const [provider, setProvider] = useState<string>('anthropic');
  useEffect(() => {
    if (props.commits) {
      setOpenStates(props.commits.map(() => false));
    }
  }, [props.commits]);

  const defaultPage = 1;
  const defaultPageSize = 20;
  const pageParam = query.get('page');
  const pageSizeParam = query.get('pageSize');
  const currentPage = pageParam ? parseInt(pageParam) : defaultPage;
  const pageSize = pageSizeParam ? parseInt(pageSizeParam) : defaultPageSize;
  const validCurrentPage = isNaN(currentPage) || currentPage < 1 ? defaultPage : currentPage;
  const validPageSize = isNaN(pageSize) || pageSize < 1 ? defaultPageSize : pageSize;

  useEffect(() => {
    handlePageChange(1, validPageSize);
  }, [selectedTags, withoutTags]);

  const handlePageChange = (page: number, size: number) => {
    const params = new URLSearchParams();
    if (page !== defaultPage) params.set('page', page.toString());
    if (size !== defaultPageSize) params.set('pageSize', size.toString());
    navigate({ search: params.toString() });

    dispatch(changeTagsPage({ page, pageSize: size, tags: selectedTags, withoutTags }));
  };

  const handleCategorizeClick = () => {
    switch (categoriesState) {
      case CategoriesState.VIEW:
        setCategoriesState(CategoriesState.SELECT);
        setSelectedCommits([]);
        break;

      case CategoriesState.SELECT:
        setCategoriesState(CategoriesState.WAITING);
        dispatch(
          requestTagCommits({
            commitShas: selectedCommits,
            provider: provider,
          }),
        );
        break;

      case CategoriesState.WAITING:
        setCategoriesState(CategoriesState.VIEW);
        setSelectedCommits([]);
        break;
    }
  };

  useEffect(() => {
    if (!props.isFetching && categoriesState === CategoriesState.WAITING) {
      setCategoriesState(CategoriesState.VIEW);
    }
  }, [props.isFetching, categoriesState]);

  let selectButton = styles.llmProviderSelector;
  if (categoriesState !== CategoriesState.SELECT) selectButton += ' ' + styles.hideSelect;

  return (
    <div className={styles.tagsContainer}>
      <div className={styles.tagsFilter}>
        <TagsFilter
          selectedTags={selectedTags}
          onTagsChange={(tags) => setSelectedTags(tags)}
          withoutTags={withoutTags}
          onWithoutTagsChange={(value) => setWithoutTags(value)}
        />
      </div>

      {props.isFetching && (
        <div className={styles.messageContainer}>
          <h1>Loading...</h1>
        </div>
      )}

      {!props.isFetching && (!props.commits || !props.commits.length) && (
        <div className={styles.messageContainer}>
          <h1>No commits to display</h1>
        </div>
      )}

      {!props.isFetching && props.commits && props.commits.length > 0 && (
        <>
          <div className={styles.modeButtons}>
            <div className={selectButton}>
              <label>
                Provider:&nbsp;
                <select
                  className={styles.modeButton}
                  value={provider}
                  onChange={(e) => setProvider(e.target.value)}
                  disabled={categoriesState === CategoriesState.WAITING}>
                  <option value="llama">Llama</option>
                  <option value="anthropic">Anthropic</option>
                </select>
              </label>
            </div>
            <button
              className={
                'button ' +
                styles.modeButton +
                ' ' +
                (categoriesState === CategoriesState.VIEW
                  ? styles.viewCategories
                  : categoriesState === CategoriesState.SELECT
                    ? styles.selectCategories
                    : categoriesState === CategoriesState.WAITING
                      ? styles.waitingForCategorization
                      : styles.errorCategory)
              }
              onClick={handleCategorizeClick}
              disabled={categoriesState === CategoriesState.WAITING}>
              {categoriesState === CategoriesState.VIEW
                ? 'Viewing Commits'
                : categoriesState === CategoriesState.SELECT
                  ? 'Selecting Commits'
                  : categoriesState === CategoriesState.WAITING
                    ? 'Waiting for Commits...'
                    : ''}
              {categoriesState === CategoriesState.VIEW ? (
                // @ts-ignore
                <status-indicator positive></status-indicator>
              ) : categoriesState === CategoriesState.SELECT ? (
                // @ts-ignore
                <status-indicator active></status-indicator>
              ) : categoriesState === CategoriesState.WAITING ? (
                // @ts-ignore
                <status-indicator intermediary pulse></status-indicator>
              ) : (
                // @ts-ignore
                <status-indicator negative></status-indicator>
              )}
            </button>
          </div>

          <div className={styles.collapsibleContainer}>
            {props.commits.map((commit, index) => {
              const isOpen = openStates[index] ?? false;

              const setIsOpenForCommit = (valueOrSetterFn: boolean | ((prev: boolean) => boolean)) => {
                setOpenStates((prev) =>
                  prev.map((val, i) =>
                    i === index
                      ? typeof valueOrSetterFn === 'function'
                        ? (valueOrSetterFn as (prevVal: boolean) => boolean)(val)
                        : valueOrSetterFn
                      : val,
                  ),
                );
              };

              const isSelected = selectedCommits.includes(commit.sha);

              return (
                <Collapsible
                  key={commit.sha}
                  title={commit.messageHeader}
                  tags={commit.tags}
                  webUrl={commit.webUrl}
                  className={styles.collapsible}
                  isInSelectMode={categoriesState === CategoriesState.SELECT}
                  isSelected={isSelected}
                  onSelectChange={(checked) => {
                    setSelectedCommits((prev) => (checked ? [...prev, commit.sha] : prev.filter((sha) => sha !== commit.sha)));
                  }}
                  isOpen={isOpen}
                  setIsOpen={setIsOpenForCommit}>
                  <div className={styles.commitDetails}>
                    <div className={styles.commitMeta}>
                      <div className={styles.shaInfo}>
                        <strong>SHA:</strong> {commit.shortSha}
                      </div>
                      <div className={styles.dateInfo}>
                        <strong>Date:</strong> {moment(commit.date).format('YYYY-MM-DD HH:mm:ss')}
                      </div>
                      <div className={styles.branchInfo}>
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 16 16"
                          fill="currentColor"
                          aria-hidden="true"
                          style={{ verticalAlign: 'text-bottom' }}>
                          {/* eslint-disable-next-line max-len */}
                          <path d="M9.5 3.25a2.25 2.25 0 1 1 3 2.122V6A2.5 2.5 0 0 1 10 8.5H6a1 1 0 0 0-1 1v1.128a2.251 2.251 0 1 1-1.5 0V5.372a2.25 2.25 0 1 1 1.5 0v1.836A2.493 2.493 0 0 1 6 7h4a1 1 0 0 0 1-1v-.628A2.25 2.25 0 0 1 9.5 3.25ZM3.5 3.25a.75.75 0 1 0 1.5 0 .75.75 0 0 0-1.5 0Zm6 0a.75.75 0 1 0 0 1.5.75.75 0 0 0 0-1.5ZM4.25 12a.75.75 0 1 0 0 1.5.75.75 0 0 0 0-1.5Z" />
                        </svg>
                        {commit.branch}
                      </div>
                    </div>
                    <div className={styles.commitMessage}>
                      <pre>{commit.message.trim()}</pre>
                    </div>
                  </div>
                </Collapsible>
              );
            })}
          </div>

          <Pagination
            totalCount={props.totalCount}
            currentPage={validCurrentPage}
            pageSize={validPageSize}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
}

export default List;
