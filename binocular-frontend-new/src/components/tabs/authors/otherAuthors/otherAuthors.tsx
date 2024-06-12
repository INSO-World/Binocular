import otherAuthorsStyles from './otherAuthors.module.scss';
import { useSelector } from 'react-redux';
import { AppDispatch, RootState, useAppDispatch } from '../../../../redux';
import { setAuthorList, setDragging } from '../../../../redux/authorsReducer.ts';

function OtherAuthors(props: { orientation?: string }) {
  const dispatch: AppDispatch = useAppDispatch();

  const authors = useSelector((state: RootState) => state.authors.authorList);
  const dragging = useSelector((state: RootState) => state.authors.dragging);

  return (
    <div className={'text-xs'}>
      <div
        className={
          otherAuthorsStyles.authorList +
          ' ' +
          (props.orientation === 'horizontal' ? otherAuthorsStyles.authorListHorizontal : otherAuthorsStyles.authorListVertical)
        }>
        <div>
          {authors
            .filter((a) => a.parent === 0)
            .map((parentAuthor, i) => {
              return (
                <div key={'author' + i}>
                  <div className={otherAuthorsStyles.authorContainer}>
                    <div
                      style={{ borderColor: parentAuthor.color.main }}
                      className={otherAuthorsStyles.authorName}
                      draggable={true}
                      onDragStart={(event) => {
                        setTimeout(() => dispatch(setDragging(true), 1));
                        event.dataTransfer.setData('draggingAuthorId', String(parentAuthor.id));
                      }}
                      onDragEnd={() => dispatch(setDragging(false))}>
                      <div style={{ background: parentAuthor.color.secondary }} className={otherAuthorsStyles.authorNameBackground}></div>
                      <div className={otherAuthorsStyles.authorNameText}>{parentAuthor.name}</div>
                    </div>
                  </div>
                </div>
              );
            })}
          {authors.filter((a) => a.parent === 0).length === 0 && <div className={'m-1'}>No Authors in Other</div>}
        </div>
      </div>
      {(dragging || props.orientation === 'horizontal') && (
        <div
          className={
            otherAuthorsStyles.authorDropOther +
            ' ' +
            (props.orientation === 'horizontal' ? otherAuthorsStyles.authorDropOtherHorizontal : otherAuthorsStyles.authorDropOtherVertical)
          }
          onDrop={(event) => {
            event.stopPropagation();
            dispatch(setDragging(false));
            dispatch(
              setAuthorList(
                authors.map((a) => {
                  if (a.parent === Number(event.dataTransfer.getData('draggingAuthorId'))) {
                    return { name: a.name, id: a.id, color: a.color, parent: 0, selected: a.selected };
                  }
                  if (a.id === Number(event.dataTransfer.getData('draggingAuthorId'))) {
                    return { name: a.name, id: a.id, color: a.color, parent: 0, selected: a.selected };
                  }
                  return a;
                }),
              ),
            );
          }}
          onDragOver={(event) => event.preventDefault()}>
          <span>Drop author here to add to other!</span>
        </div>
      )}
    </div>
  );
}

export default OtherAuthors;
