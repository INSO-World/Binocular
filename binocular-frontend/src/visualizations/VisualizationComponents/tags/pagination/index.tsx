import React from 'react';
import styles from './pagination.module.scss';

interface PaginationProps {
  totalCount: number;
  currentPage: number;
  pageSize: number;
  onPageChange: (page: number, pageSize: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ totalCount, currentPage, pageSize, onPageChange }) => {
  const totalPages = Math.ceil(totalCount / pageSize) || 1;

  const pageNumbers: number[] = [];
  const maxPageNumbersToShow = 5;
  let startPage = Math.max(1, currentPage - Math.floor(maxPageNumbersToShow / 2));
  let endPage = startPage + maxPageNumbersToShow - 1;

  if (endPage > totalPages) {
    endPage = totalPages;
    startPage = Math.max(1, endPage - maxPageNumbersToShow + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  const goToPage = (page: number) => {
    onPageChange(page, pageSize);
  };

  const handlePageSizeChange = (newPageSize: number) => {
    onPageChange(currentPage, newPageSize);
  };

  return (
    <div className={styles.paginationContainer}>
      <ul className={styles.pagination}>
        <li>
          <button onClick={() => goToPage(1)} disabled={currentPage === 1}>
            <span className={styles.skipToStart}>|</span>&lt;
          </button>
        </li>
        <li>
          <button onClick={() => goToPage(currentPage - 1)} disabled={currentPage === 1}>
            &lt;
          </button>
        </li>

        {pageNumbers.map((pageNum) => (
          <li key={pageNum} className={pageNum === currentPage ? styles.active : ''}>
            <button onClick={() => goToPage(pageNum)} disabled={pageNum === currentPage}>
              {pageNum}
            </button>
          </li>
        ))}

        <li>
          <button onClick={() => goToPage(currentPage + 1)} disabled={currentPage === totalPages}>
            &gt;
          </button>
        </li>
        <li>
          <button onClick={() => goToPage(totalPages)} disabled={currentPage === totalPages}>
            &gt;<span className={styles.skipToEnd}>|</span>
          </button>
        </li>
      </ul>

      <div className={styles.pageSizeSelector}>
        <label htmlFor="pageSizeSelect">Per page: </label>
        <select id="pageSizeSelect" value={pageSize} onChange={(e) => handlePageSizeChange(parseInt(e.target.value))}>
          {[10, 20, 50, 100].map((size: number) => (
            <option key={size} value={size}>
              {size}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
};

export default Pagination;
