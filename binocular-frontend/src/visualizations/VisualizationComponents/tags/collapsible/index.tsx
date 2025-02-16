'use strict';

import React, { useRef } from 'react';
import styles from './collapsible.module.scss';

interface IProps {
  open?: boolean;
  children?: React.ReactNode;
  title: string;
  className?: string;
  tags?: string[];
  webUrl?: string;
  isInSelectMode?: boolean;
  isOpen: boolean;
  setIsOpen: React.Dispatch<React.SetStateAction<boolean>>;
  isSelected?: boolean;
  onSelectChange?: (checked: boolean) => void;
}

const Collapsible: React.FC<IProps> = ({
  children,
  title,
  className,
  tags,
  webUrl,
  isInSelectMode,
  isOpen,
  setIsOpen,
  isSelected,
  onSelectChange,
}) => {
  const contentRef = useRef<HTMLDivElement>(null);

  const handleFilterOpening = () => {
    setIsOpen((prev) => !prev);
  };

  const handleSelectingCommit = () => {
    if (onSelectChange) {
      onSelectChange(!isSelected);
    }
  };

  const headerClasses = [
    styles.header,
    isInSelectMode ? (isSelected ? styles.isSelected : styles.isInSelectMode) : '',
    className || '',
  ].join(' ');

  return (
    <>
      <div className={`${styles.collapsible}`}>
        <div
          className={headerClasses}
          onClick={isInSelectMode ? handleSelectingCommit : handleFilterOpening}
          role="button"
          aria-expanded={isOpen}
          tabIndex={0}>
          <div className={styles.titleContainer}>
            {webUrl ? (
              <a href={webUrl} target="_blank" rel="noopener noreferrer" className={styles.titleLink} onClick={(e) => e.stopPropagation()}>
                <span className={styles.title}>{title}</span>
              </a>
            ) : (
              <span className={styles.title}>{title}</span>
            )}
          </div>

          <div className={styles.rightSide}>
            {tags && tags.length > 0 && (
              <div className={styles.tagsContainer}>
                {tags.map((tag) => (
                  <span key={tag} className={`${styles.tag}`}>
                    {tag}
                  </span>
                ))}
              </div>
            )}
            <span className={styles.icon} aria-hidden="true">
              {isOpen ? '▲' : '▼'}
            </span>
          </div>
        </div>
      </div>
      <div className={`${styles.content} ${isOpen ? styles.expanded : ''}`} ref={contentRef}>
        {children}
      </div>
    </>
  );
};

export default Collapsible;
