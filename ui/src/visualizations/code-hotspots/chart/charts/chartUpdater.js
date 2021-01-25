import HunkHandler from '../helper/hunkHandler';
import chartGeneration from './chartGeneration';

import _ from 'lodash';


let storedData;
let storedLines;
let storedCommits;
let storedDevs;
let storedIssues;
let storedMaxValue;
let storedLegendSteps;

export default class chartUpdater {
  static updateAllChartsWithChangesPerVersion(rawData,lines,path,currThis,updateData){
    if(updateData){
      let data=[];
      let commits=0;

      let legendSteps = 20;

      let maxValue =0;


      for (let i in rawData.data) {
        let commit = rawData.data[i];
        for (let j = 0; j < lines; j++) {
          data.push({"column":i,"row":j,"value":0,"message":commit.message,"sha":commit.sha,"signature":commit.signature})
        }


        let files = commit.files.data;
        let file = _.filter(files, {file:{path: path}})[0];
        if(file!==undefined){
          for (let j in file.hunks) {
            let hunk = file.hunks[j];
            let tmpMaxValue = HunkHandler.handle(hunk,data,i,maxValue);
            if(tmpMaxValue>maxValue){
              maxValue=tmpMaxValue;
            }
          }
          commits++;
        }
      }
      this.storedData = data;
      this.storedLines = lines;
      this.storedCommits = commits;
      this.storedMaxValue = maxValue;
      this.storedLegendSteps = legendSteps;

    }

    chartGeneration.generateRowSummary(this.storedData,this.storedLines,currThis,0,this.storedLegendSteps);
    chartGeneration.generateHeatmap(this.storedData,this.storedLines,this.storedCommits,currThis,0,this.storedMaxValue,this.storedLegendSteps);
    chartGeneration.generateBarChart(this.storedData,this.storedCommits,currThis,0,this.storedLegendSteps);
  }

  static updateAllChartsWithChangesPerDeveloper(rawData,lines,path,currThis,updateData){
    if(updateData) {
      let data=[];

      let legendSteps = 20;

      let maxValue =0;
      let devs = [];

      for (let commit of rawData.data) {
        if(!devs.includes(commit.signature)){
          devs.push(commit.signature);
        }
      }

      for (let i in devs){
        for (let j = 0; j < lines; j++) {
          data.push({"column":i,"row":j,"value":0,"message":"","sha":"","dev":devs[i]})
        }
      }
      console.log(devs);

      for (let i in rawData.data) {
        let commit = rawData.data[i];
        let files = commit.files.data;
        let file = _.filter(files, {file:{path: path}})[0];
        if(file!==undefined){
          for (let j in file.hunks) {
            let hunk = file.hunks[j];
            let tmpMaxValue = HunkHandler.handle(hunk,data,devs.indexOf(commit.signature),maxValue);
            if(tmpMaxValue>maxValue){
              maxValue=tmpMaxValue;
            }
          }
        }
      }
      this.storedData = data;
      this.storedLines = lines;
      this.storedDevs = devs;
      this.storedMaxValue = maxValue;
      this.storedLegendSteps = legendSteps;
    }

    chartGeneration.generateRowSummary(this.storedData,this.storedLines,currThis,1,this.storedLegendSteps);
    chartGeneration.generateHeatmap(this.storedData,this.storedLines,this.storedDevs.length,currThis,1,this.storedMaxValue,this.storedLegendSteps);
    chartGeneration.generateBarChart(this.storedData,this.storedDevs.length,currThis,1,this.storedLegendSteps);

  }

  static updateAllChartsWithChangesPerIssue(rawData,lines,path,currThis,updateData){
    if(updateData) {
      let data=[];

      let legendSteps = 20;

      let maxValue =0;
      let issues = [];
      let issuesDescriptions = [];
      let issuesIID = [];


      for (let issue of rawData.data) {
        if(!issues.includes(issue.title)){
          issues.push(issue.title);
          issuesDescriptions.push(issue.description);
          issuesIID.push(issue.iid);

        }
      }

      for (let i in issues){
        for (let j = 0; j < lines; j++) {
          data.push({"column":i,"row":j,"value":0,"message":"","sha":"","title":issues[i],"description":issuesDescriptions[i],"iid":issuesIID[i]})
        }
      }
      for (let i in rawData.data) {
        let issue = rawData.data[i];
        for (let j in issue.commits.data) {
          let commit = issue.commits.data[j];
          let file = commit.file;
          if(file!==null){
            for (let k in file.hunks) {
              let hunk = file.hunks[k];
              let tmpMaxValue = HunkHandler.handle(hunk,data,issues.indexOf(issue.title),maxValue);
              if(tmpMaxValue>maxValue){
                maxValue=tmpMaxValue;
              }
            }
          }
        }
      }

      this.storedData = data;
      this.storedLines = lines;
      this.storedIssues = issues;
      this.storedMaxValue = maxValue;
      this.storedLegendSteps = legendSteps;
    }
    chartGeneration.generateRowSummary(this.storedData,this.storedLines,currThis,2,this.storedLegendSteps);
    chartGeneration.generateHeatmap(this.storedData,this.storedLines,this.storedIssues.length,currThis,2,this.storedMaxValue,this.storedLegendSteps);
    chartGeneration.generateBarChart(this.storedData,this.storedIssues.length,currThis,2,this.storedLegendSteps);

  }
}
