import React, { useCallback } from 'react';
import Select, { MultiValue } from 'react-select';
import styles from './tagsFilter.module.scss';

import tagsConfig from '../../../../../../config/tagsConfig.json';

const tagOptions = tagsConfig.possibleTags.map((tag) => ({
  value: tag,
  label: tag,
}));

interface TagOption {
  value: string;
  label: string;
}

export interface TagsFilterProps {
  selectedTags: string[];
  onTagsChange: (tags: string[]) => void;
  withoutTags: boolean;
  onWithoutTagsChange: (value: boolean) => void;
  isDisabled?: boolean;
}

const TagsFilter: React.FC<TagsFilterProps> = ({ selectedTags, onTagsChange, withoutTags, onWithoutTagsChange, isDisabled = false }) => {
  const selectedTagOptions: TagOption[] = selectedTags.map((t) => ({ value: t, label: t }));

  const handleChange = useCallback(
    (newValue: MultiValue<TagOption>) => {
      const newTags = newValue.map((item) => item.value);
      onTagsChange(newTags);
    },
    [onTagsChange],
  );

  const handleWithoutTags = useCallback(() => {
    onWithoutTagsChange(!withoutTags);
  }, [withoutTags, onWithoutTagsChange]);

  return (
    <div className={styles.tagsFilterContainer}>
      <Select
        isMulti
        isDisabled={isDisabled}
        value={selectedTagOptions}
        onChange={handleChange}
        options={tagOptions}
        placeholder="Select tags..."
        className="tags-select"
        classNamePrefix="tags-select"
      />
      <div className={styles.checkbox}>
        <label>
          <input type="checkbox" checked={withoutTags} onChange={handleWithoutTags} />
          &nbsp; Show commits without tags
        </label>
      </div>
    </div>
  );
};

export default TagsFilter;
