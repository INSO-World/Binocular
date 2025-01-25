import { Counters, SunburstData } from '../chart/chart.tsx';
import { DataPluginJacocoReport } from '../../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';

/**
 * Converts JaCoCo XML report to a SunburstData object
 */
export function convertJacocoReportDataToSunburstChartData(data: DataPluginJacocoReport): SunburstData {
  const parser = new DOMParser();
  const xmlDoc = parser.parseFromString(data.xmlContent, 'application/xml');

  const report = xmlDoc.getElementsByTagName('report');
  const rootNode: SunburstData = { name: (report[0]?.getAttribute('name') || 'Jacoco report') + ': ', children: [] };
  const classes = xmlDoc.getElementsByTagName('class');

  // Iterate over all classes in the report
  for (let i = 0; i < classes.length; i++) {
    const classElement = classes[i];
    const className = classElement.getAttribute('name') || 'Unknown Class';

    const methods = classElement.getElementsByTagName('method');

    // Iterate over all methods in the class
    for (let j = 0; j < methods.length; j++) {
      const methodElement = methods[j];
      const methodName = methodElement.getAttribute('name') || 'Unknown Method';

      const counters: Counters = {};
      const counterElements = methodElement.getElementsByTagName('counter');

      for (let k = 0; k < counterElements.length; k++) {
        const counterElement = counterElements[k];
        const type = counterElement.getAttribute('type');
        const missed = parseInt(counterElement.getAttribute('missed') || '0');
        const covered = parseInt(counterElement.getAttribute('covered') || '0');

        if (type) {
          counters[type as keyof Counters] = { missed, covered };
        }
      }

      const methodNode: SunburstData = {
        name: methodName,
        counters: [counters],
        children: undefined,
      };

      // Insert the method into the hierarchical package structure
      insertIntoHierarchy(rootNode, className.split('/'), methodNode);
    }
  }
  return rootNode;
}

/**
 * Recursive function to insert all methods from each class into hierarchy in SunburstData format.
 */
function insertIntoHierarchy(root: SunburstData, classPath: string[], classNode: SunburstData) {
  if (classPath.length === 0) return;

  const currentPart = classPath[0];
  let childNode = root.children?.find((child) => child.name === currentPart);

  if (!childNode) {
    childNode = { name: currentPart, children: [] };
    if (!root.children) root.children = [];
    root.children.push(childNode);
  }

  if (classPath.length > 1) {
    insertIntoHierarchy(childNode, classPath.slice(1), classNode);
  } else {
    if (!childNode.children) childNode.children = [];
    childNode.children.push(classNode);
  }
}
